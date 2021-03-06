/**
 * created by ms on 04.11.2019
 */
import React from "react";
import { connect } from "react-redux";
import SwipeableViews from "react-swipeable-views";
import "~/style/_atom-context-layout.scss";

import WonAtomHeader from "./atom-header.jsx";
import PropTypes from "prop-types";

const mapStateToProps = (state, ownProps) => {
  return {
    atomUri: ownProps.atomUri,
    onClick: ownProps.onClick,
    actionButtons: ownProps.actionButtons ? ownProps.actionButtons : undefined,
    className: ownProps.className,
    enableMouseEvents: false,
    hideTimestamp: ownProps.hideTimestamp ? ownProps.hideTimestamp : false,
  };
};

/*const mapDispatchToProps = dispatch => {
  return {};
};*/

class WonAtomContextSwipeableView extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      show: false,
    };
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick(show) {
    this.setState({
      show: !show,
    });
  }

  render() {
    const headerElement = (
      <WonAtomHeader
        atomUri={this.props.atomUri}
        hideTimestamp={this.props.hideTimestamp}
        onClick={this.props.onClick}
      />
    );

    let buttons = this.props.actionButtons;

    if (this.props.actionButtons) {
      const show = this.state.show;
      buttons = (
        <div onClick={() => this.handleClick(show)}>
          {this.props.actionButtons}
        </div>
      );

      let triggerIcon = (
        <React.Fragment>
          <svg
            className="cl__trigger cl__trigger--waiting"
            onClick={() => this.handleClick(show)}
          >
            <use xlinkHref="#ico16_contextmenu" href="#ico16_contextmenu" />
          </svg>
          <svg
            className="cl__trigger cl__trigger--add"
            onClick={() => this.handleClick(show)}
          >
            <use xlinkHref="#ico32_buddy_add" href="#ico32_buddy_add" />
          </svg>
          <svg
            className="cl__trigger cl__trigger--default"
            onClick={() => this.handleClick(show)}
          >
            <use xlinkHref="#ico16_contextmenu" href="#ico16_contextmenu" />
          </svg>
        </React.Fragment>
      );

      return (
        <won-atom-context-layout class={this.props.className}>
          <div className="cl__main ">
            <SwipeableViews
              index={show ? 1 : 0}
              enableMouseEvents={this.props.enableMouseEvents}
            >
              {headerElement}
              {buttons}
            </SwipeableViews>
          </div>
          {triggerIcon}
        </won-atom-context-layout>
      );
    } else {
      return headerElement;
    }
  }
}

WonAtomContextSwipeableView.propTypes = {
  atomUri: PropTypes.string,
  onClick: PropTypes.func,
  actionButtons: PropTypes.object,
  className: PropTypes.string,
  enableMouseEvents: PropTypes.bool,
  hideTimestamp: PropTypes.bool,
};

export default connect(mapStateToProps)(WonAtomContextSwipeableView);
