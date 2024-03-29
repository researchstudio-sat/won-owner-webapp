/**
 * Created by quasarchimaere on 30.07.2019.
 */
import React from "react";
import Immutable from "immutable";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { get, getIn } from "../../utils.js";
import { actionCreators } from "../../actions/actions.js";
import * as atomUtils from "../../redux/utils/atom-utils.js";

import "~/style/_persona-card.scss";

const mapStateToProps = (state, ownProps) => {
  const atom = getIn(state, ["atoms", ownProps.atomUri]);
  const identiconSvg = atomUtils.getIdenticonSvg(atom);
  const atomImage = atomUtils.getDefaultPersonaImage(atom);

  return {
    atomUri: ownProps.atomUri,
    onAtomClick: ownProps.onAtomClick,
    isInactive: atomUtils.isInactive(atom),
    atom,
    personaName: get(atom, "humanReadable"),
    atomImage,
    showDefaultIcon: !atomImage,
    identiconSvg,
  };
};

const mapDispatchToProps = dispatch => {
  return {
    routerGo: (path, props) => {
      dispatch(actionCreators.router__stateGo(path, props));
    },
    selectAtomTab: (atomUri, selectTab) => {
      dispatch(
        actionCreators.atoms__selectTab(
          Immutable.fromJS({
            atomUri: atomUri,
            selectTab: selectTab,
          })
        )
      );
    },
  };
};

class WonPersonaCard extends React.Component {
  constructor(props) {
    super(props);
    this.atomClick = this.atomClick.bind(this);
  }

  render() {
    const personaIdenticon =
      this.props.showDefaultIcon && this.props.identiconSvg ? (
        <img
          className="identicon"
          alt="Auto-generated title image"
          src={"data:image/svg+xml;base64," + this.props.identiconSvg}
        />
      ) : (
        undefined
      );

    const personaImage = this.props.atomImage ? (
      <img
        className="image"
        alt={this.props.atomImage.get("name")}
        src={
          "data:" +
          this.props.atomImage.get("type") +
          ";base64," +
          this.props.atomImage.get("data")
        }
      />
    ) : (
      undefined
    );

    return (
      <won-persona-card onClick={this.atomClick}>
        <div
          className={
            "card__icon clickable " + (this.props.isInactive ? "inactive" : "")
          }
        >
          {personaIdenticon}
          {personaImage}
        </div>
        <div className="card__main clickable">
          <div className="card__main__name">{this.props.personaName}</div>
        </div>
      </won-persona-card>
    );
  }

  atomClick() {
    if (this.props.onAtomClick) {
      this.props.onAtomClick();
    } else {
      this.props.selectAtomTab(this.props.atomUri, "DETAIL");
      this.props.routerGo("post", { postUri: this.props.atomUri });
    }
  }
}
WonPersonaCard.propTypes = {
  atomUri: PropTypes.string.isRequired,
  onAtomClick: PropTypes.func,
  selectAtomTab: PropTypes.func,
  routerGo: PropTypes.func,
  atom: PropTypes.object,
  isInactive: PropTypes.bool,
  personaName: PropTypes.string,
  atomImage: PropTypes.string,
  showDefaultIcon: PropTypes.bool,
  identiconSvg: PropTypes.string,
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(WonPersonaCard);
