import React from "react";
import PropTypes from "prop-types";
import ElmComponent from "react-elm-components";
import { actionCreators } from "../actions/actions.js";

import "./svg-icon.js";

import "../../style/_elm.scss";

export default class ElmReact extends React.Component {
  componentWillUnmount() {
    if (this.state && this.state.ports) {
      this.state.ports.outPort && this.state.ports.outPort.unsubscribe();
      this.state.ports.errorPort.unsubscribe();
      this.state.ports.inPort.send({
        unmount: true,
      });
    }
  }

  render() {
    return (
      <ElmComponent
        src={this.props.src}
        flags={this.props.flags}
        ports={this.setupPorts.bind(this)}
      />
    );
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (this.state && this.state.ports && nextProps.flags) {
      this.state.ports.inPort.send({
        newProps: nextProps.flags,
      });
    }
  }

  setupPorts(ports) {
    ports.errorPort.subscribe(error => {
      console.error(error);
    });

    if (ports.outPort) {
      ports.outPort.subscribe(message => {
        switch (message.type) {
          case "action":
            if (actionCreators[message.name]) {
              this.props.ngRedux.dispatch(
                actionCreators[message.name](...message.arguments)
              );
            } else {
              console.error(`Could not find action "${message.name}"`);
            }
            break;
          case "event": {
            const eventAttrName = message.name
              .replace(/([A-Z])/g, "-$1")
              .toLowerCase();
            if (typeof this.props[eventAttrName] === "function") {
              this.props[eventAttrName](message.payload);
            } else {
              console.error(
                `Could not find eventHandler ${eventAttrName}`,
                this.props
              );
            }
            break;
          }
          default:
            console.error(`Could not read message "${message}"`);
        }
      });
    }
    this.setState({ ports });
  }
}
ElmReact.propTypes = {
  src: PropTypes.object.isRequired,
  flags: PropTypes.object,
  ngRedux: PropTypes.object.isRequired,
};
