/**
 * Рекомендация JAICP ES6: алиас $ к контексту через global + Proxy.
 * get-ловушка читает актуальный контекст через $jsapi.context() (как раньше __noSuchProperty__).
 */
var dollarProxy = new Proxy(
  {},
  {
    get: function (target, name) {
      if (typeof $jsapi !== "undefined" && $jsapi.context) {
        return $jsapi.context()[name];
      }
      if (typeof $context !== "undefined") {
        return $context[name];
      }
      return undefined;
    },
  }
);

if (typeof globalThis !== "undefined") {
  globalThis.$ = dollarProxy;
}
if (typeof global !== "undefined") {
  global.$ = dollarProxy;
}
if (typeof $global !== "undefined") {
  $global.$ = dollarProxy;
}

export default {};
