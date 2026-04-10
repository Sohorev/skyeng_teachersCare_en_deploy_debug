import _ from "lodash";

function injectorCurrentLanguage() {
  if (typeof $injector !== "undefined" && $injector && $injector.currentLanguage) {
    return $injector.currentLanguage;
  }
  return "unknown";
}

function injectorTokenChatApi() {
  if (typeof $injector !== "undefined" && $injector && $injector.tokenChatApi) {
    return $injector.tokenChatApi;
  }
  return "did not get token";
}

/**
 * Язык запроса для аналитики (async: $caila.detectLanguage в ES6).
 */
async function analyticsLangAsync(request) {
  if (!request) {
    return injectorCurrentLanguage();
  }
  var text = String(request).replace(/\n/g, " ");
  var langs = await $caila.detectLanguage([text]);
  var lang = langs && langs[0];
  return _.includes(["ru", "en"], lang) ? lang : "unknown";
}

/**
 * Аналог formResponseData из legacy functions.js с await detectLanguage.
 */
async function buildFormResponseData(session, request, intent) {
  var language = await analyticsLangAsync(request);
  return {
    token: injectorTokenChatApi(),
    channel: session.channel,
    locale: session.locale,
    lessonNow: session.lessonNow,
    language: language,
    scenario: intent || "",
    scenarios: session.scenarios || [],
    sessionScore: session.score || null,
  };
}

/**
 * Аналог setComment с await analyticsLangAsync.
 */
async function setCommentAsync(session, scenario, request, isTargetState) {
  if (typeof $ === "undefined" || !$ || !$.temp) {
    return;
  }
  if (!$.temp.analCommSet) {
    var lang = await analyticsLangAsync(request);
    if (typeof $analytics !== "undefined") {
      $analytics.setComment(
        "scenario: " + (scenario || "—") +
          ", lang: " + lang +
          ", lessonNow: " + session.lessonNow +
          ", targetState: " + isTargetState
      );
    }
    if (isTargetState) {
      session.targetStateCounter = ++session.targetStateCounter || 1;
    }
  }
  $.temp.analCommSet = true;
}

/** В import.sc: name = formResponseDataAsync */
export default {
  analyticsLangAsync,
  buildFormResponseData,
  setCommentAsync,
};
