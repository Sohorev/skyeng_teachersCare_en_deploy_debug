/**
 * postProcess из main.sc: парсер .sc не допускает async function в handler:,
 * поэтому async-логика здесь, а в init остаётся handler: function() { return ...; }
 */
import _ from "lodash";

async function forThematicPostProcess() {
  $.response.replies = $.response.replies || [];
  if (isThematic($.currentState)) {
    var currentScenario = stateToIntent[$.currentState];
    if (!_.findWhere($.response.replies, { type: "text" })) {
      $.response.replies.unshift({ type: "text", text: "a24.000.004en" });
    }
    if (
      !_.findWhere($.response.replies, { type: "buttons" }) &&
      _.findWhere($.response.replies, { type: "text" })
    ) {
      $reactions.transition("/IssueResolved/Ask");
      await formResponseDataAsync.setCommentAsync(
        $.session,
        currentScenario,
        $.request.query,
        true
      );
    } else {
      await formResponseDataAsync.setCommentAsync(
        $.session,
        currentScenario,
        $.request.query,
        false
      );
    }
    if (currentScenario) $.session.scenarios.push(currentScenario);
  } else {
    await formResponseDataAsync.setCommentAsync($.session, "", $.request.query, false);
  }
}

async function answerChangerPostProcess() {
  $.response.replies = $.response.replies || [];
  $.response.replies.forEach(function (answer) {
    if (
      answer.type === "text" &&
      String(answer.text).match($global.answerId) &&
      String(answer.text).match($global.answerId)[0] === answer.text &&
      !testMode()
    ) {
      answer.text = answers[answer.text];
    } else if (answer.type === "buttons") {
      var newButtons = [];
      answer.buttons.forEach(function (button) {
        var matched = String(button.text).match($global.buttonId);
        if (matched && matched[0] === button.text && !testMode()) {
          if (Array.isArray(buttons[button.text])) {
            buttons[button.text].forEach(function (buttonData) {
              newButtons.push({
                text: buttonData.text,
                transition: buttonData.transition,
              });
            });
          }
        } else {
          newButtons.push({
            text: button.text,
            transition: button.transition,
          });
        }
      });
      answer.buttons = newButtons;
    }
  });
  if (!$.response.data) {
    $.response.data = await formResponseDataAsync.buildFormResponseData(
      $.session,
      $.request.query,
      stateToIntent[$.currentState]
    );
  }
}

/*export default {
  forThematicPostProcess,
  answerChangerPostProcess
}*/

var teachersCareMainPostProcess = {
  forThematicPostProcess,
  answerChangerPostProcess,
};

export default teachersCareMainPostProcess;

if (typeof $global !== "undefined") {
  $global.teachersCareMainPostProcess = teachersCareMainPostProcess;
}
if (typeof globalThis !== "undefined") {
  globalThis.teachersCareMainPostProcess = teachersCareMainPostProcess;
}
if (typeof global !== "undefined") {
  global.teachersCareMainPostProcess = teachersCareMainPostProcess;
}
