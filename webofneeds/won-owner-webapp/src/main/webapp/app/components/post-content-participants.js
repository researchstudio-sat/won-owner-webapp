/**
 * Created by quasarchimaere on 25.03.2019.
 */

import angular from "angular";
import inviewModule from "angular-inview";
import labelledHrModule from "./labelled-hr.js";
import postHeaderModule from "./post-header.js";
import { attach, getIn, get } from "../utils.js";
import won from "../won-es6.js";
import { connect2Redux } from "../won-utils.js";
import * as needUtils from "../need-utils.js";
import * as connectionSelectors from "../selectors/connection-selectors.js";
import { actionCreators } from "../actions/actions.js";
import ngAnimate from "angular-animate";

import "style/_post-content-participants.scss";

const CONNECTION_READ_TIMEOUT = 1500;

const serviceDependencies = ["$ngRedux", "$scope", "$element"];
function genComponentConf() {
  let template = `
      <div
          class="pc-participants__participant"
          ng-if="!self.isOwned && self.hasGroupMembers"
          ng-repeat="memberUri in self.groupMembersArray track by memberUri">
          <div class="pc-participants__participant__indicator"></div>
          <won-post-header
            class="clickable"
            ng-click="self.router__stateGoCurrent({viewNeedUri: memberUri, viewConnUri: undefined})"
            need-uri="::memberUri">
          </won-post-header>
          <div class="pc-participants__participant__actions"></div>
      </div>
      <div class="pc-participants__participant"
          ng-if="self.isOwned && self.hasGroupChatConnections && conn.get('state') !== self.won.WON.Closed"
          ng-repeat="conn in self.groupChatConnectionsArray"
          in-view="conn.get('unread') && $inview && self.markAsRead(conn)"
          ng-class="{'won-unread': conn.get('unread')}">
          <div class="pc-participants__participant__indicator"></div>
          <won-post-header
            class="clickable"
            ng-click="self.router__stateGoCurrent({viewNeedUri: conn.get('remoteNeedUri'), viewConnUri: undefined})"
            need-uri="::conn.get('remoteNeedUri')">
          </won-post-header>
          <div class="pc-participants__participant__actions">
              <div
                class="pc-participants__participant__actions__button red won-button--outlined thin"
                ng-click="self.openRequest(conn)"
                ng-if="conn.get('state') === self.won.WON.RequestReceived">
                  Accept
              </div>
              <div
                class="pc-participants__participant__actions__button red won-button--outlined thin"
                ng-click="self.closeConnection(conn)"
                ng-if="conn.get('state') === self.won.WON.RequestReceived">
                  Reject
              </div>
              <div
                class="pc-participants__participant__actions__button red won-button--outlined thin"
                ng-click="self.sendRequest(conn)"
                ng-if="conn.get('state') === self.won.WON.Suggested">
                  Request
              </div>
              <div
                class="pc-participants__participant__actions__button red won-button--outlined thin"
                ng-disabled="true"
                ng-if="conn.get('state') === self.won.WON.RequestSent">
                  Waiting for Accept...
              </div>
              <div
                class="pc-participants__participant__actions__button red won-button--outlined thin"
                ng-click="self.closeConnection(conn)"
                ng-if="conn.get('state') === self.won.WON.Suggested || conn.get('state') === self.won.WON.Connected">
                  Remove
              </div>
          </div>
      </div>
      <div class="pc-participants__empty"
          ng-if="(!self.isOwned && !self.hasGroupMembers) || (self.isOwned && !self.hasGroupChatConnections)">
          No Groupmembers present.
      </div>
    `;

  class Controller {
    constructor() {
      attach(this, serviceDependencies, arguments);
      this.won = won;
      window.postcontentparticipants4dbg = this;

      const selectFromState = state => {
        const post = getIn(state, ["needs", this.postUri]);
        const isOwned = needUtils.isOwned(post);

        const hasGroupFacet = needUtils.hasGroupFacet(post);

        const groupMembers = hasGroupFacet && get(post, "groupMembers");
        const groupChatConnections =
          isOwned &&
          hasGroupFacet &&
          connectionSelectors.getGroupChatConnectionsByNeedUri(
            state,
            this.postUri
          );

        return {
          post,
          isOwned,
          hasGroupFacet,
          hasGroupMembers: groupMembers && groupMembers.size > 0,
          hasGroupChatConnections:
            groupChatConnections && groupChatConnections.size > 0,
          groupChatConnectionsArray:
            groupChatConnections && groupChatConnections.toArray(),
          groupMembersArray: groupMembers && groupMembers.toArray(),
        };
      };
      connect2Redux(selectFromState, actionCreators, ["self.postUri"], this);
    }

    closeConnection(conn, rateBad = false) {
      if (!conn) {
        return;
      }

      const connUri = conn.get("uri");

      if (rateBad) {
        this.connections__rate(connUri, won.WON.binaryRatingBad);
      }

      if (conn.get("unread")) {
        this.connections__markAsRead({
          connectionUri: connUri,
          needUri: this.postUri,
        });
      }

      this.connections__close(connUri);
    }

    openRequest(conn, message = "") {
      if (!conn) {
        return;
      }

      const connUri = get(conn, "uri");

      if (conn.get("unread")) {
        this.connections__markAsRead({
          connectionUri: connUri,
          needUri: this.postUri,
        });
      }

      this.connections__open(connUri, message);
    }

    sendRequest(conn, message = "") {
      if (!conn) {
        return;
      }

      const connUri = get(conn, "uri");
      const remoteNeedUri = get(conn, "remoteNeedUri");

      if (conn.get("unread")) {
        this.connections__markAsRead({
          connectionUri: connUri,
          needUri: this.postUri,
        });
      }

      this.connections__rate(connUri, won.WON.binaryRatingGood);
      this.needs__connect(this.postUri, connUri, remoteNeedUri, message);
    }

    markAsRead(conn) {
      if (conn && conn.get("unread")) {
        const payload = {
          connectionUri: conn.get("uri"),
          needUri: this.postUri,
        };

        const tmp_connections__markAsRead = this.connections__markAsRead;

        setTimeout(function() {
          tmp_connections__markAsRead(payload);
        }, CONNECTION_READ_TIMEOUT);
      }
    }
  }

  Controller.$inject = serviceDependencies;
  return {
    restrict: "E",
    controller: Controller,
    controllerAs: "self",
    bindToController: true, //scope-bindings -> ctrl
    template: template,
    scope: {
      postUri: "=",
    },
  };
}

export default angular
  .module("won.owner.components.postContentParticipants", [
    ngAnimate,
    labelledHrModule,
    postHeaderModule,
    inviewModule.name,
  ])
  .directive("wonPostContentParticipants", genComponentConf).name;
