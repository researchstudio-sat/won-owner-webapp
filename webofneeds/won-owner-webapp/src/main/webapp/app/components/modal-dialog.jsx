import React from "react";
import PropTypes from "prop-types";
import { connect } from "react-redux";
import { get, getIn } from "../utils.js";

import "~/style/_modal-dialog.scss";

const mapStateToProps = state => {
  const modalDialog = getIn(state, ["view", "modalDialog"]);
  const modalDialogCaption = get(modalDialog, "caption");
  const modalDialogText = get(modalDialog, "text");
  const modalDialogButtons = get(modalDialog, "buttons");

  const showTerms = get(modalDialog, "showTerms");
  return {
    modalDialogCaption,
    modalDialogText,
    showTerms,
    modalDialogButtons: modalDialogButtons ? modalDialogButtons.toArray() : [],
  };
};

class WonModalDialog extends React.Component {
  render() {
    return (
      <won-modal-dialog>
        <div className="md__dialog">
          {this.props.showTerms ? (
            <React.Fragment>
              <div className="md__dialog__header">
                <span className="md__dialog__header__caption">
                  Important Note
                </span>
              </div>
              <div className="md__dialog__content">
                <span className="md__dialog__content__text">
                  This action requires an account. If you want to proceed, we
                  will create an anonymous account for you.
                  <br />
                  <br />
                  {"By clicking 'Yes', you accept the "}
                  <a
                    target="_blank"
                    rel="noopener noreferrer"
                    href="#!/about?aboutSection=aboutTermsOfService"
                  >
                    Terms Of Service(ToS)
                  </a>
                  {
                    " and anonymous account will be created. Clicking 'No' will just cancel the action."
                  }
                </span>
              </div>
            </React.Fragment>
          ) : (
            <React.Fragment>
              <div className="md__dialog__header">
                <span className="md__dialog__header__caption">
                  {this.props.modalDialogCaption}
                </span>
              </div>
              <div className="md__dialog__content">
                <span className="md__dialog__content__text">
                  {this.props.modalDialogText}
                </span>
              </div>
            </React.Fragment>
          )}
          <div className="md__dialog__footer">
            {this.props.modalDialogButtons.map((button, index) => (
              <button
                key={get(button, "caption") + "-" + index}
                className="won-button--filled lighterblue"
                onClick={get(button, "callback")}
              >
                <span>{get(button, "caption")}</span>
              </button>
            ))}
          </div>
        </div>
      </won-modal-dialog>
    );
  }
}
WonModalDialog.propTypes = {
  modalDialogCaption: PropTypes.string,
  modalDialogText: PropTypes.string,
  showTerms: PropTypes.bool,
  modalDialogButtons: PropTypes.arrayOf(PropTypes.object),
};

export default connect(mapStateToProps)(WonModalDialog);
