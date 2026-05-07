import {
  s
} from "./chunk-GFKAIPHP.js";

// node_modules/@primeuix/utils/dist/object/index.mjs
var ie = Object.defineProperty;
var K = Object.getOwnPropertySymbols;
var se = Object.prototype.hasOwnProperty;
var ae = Object.prototype.propertyIsEnumerable;
var N = (e, t, n) => t in e ? ie(e, t, { enumerable: true, configurable: true, writable: true, value: n }) : e[t] = n;
var d = (e, t) => {
  for (var n in t || (t = {})) se.call(t, n) && N(e, n, t[n]);
  if (K) for (var n of K(t)) ae.call(t, n) && N(e, n, t[n]);
  return e;
};
function l(e) {
  return e == null || e === "" || Array.isArray(e) && e.length === 0 || !(e instanceof Date) && typeof e == "object" && Object.keys(e).length === 0;
}
function c(e) {
  return typeof e == "function" && "call" in e && "apply" in e;
}
function s2(e) {
  return !l(e);
}
function i(e, t = true) {
  return e instanceof Object && e.constructor === Object && (t || Object.keys(e).length !== 0);
}
function $(e = {}, t = {}) {
  let n = d({}, e);
  return Object.keys(t).forEach((o) => {
    let r = o;
    i(t[r]) && r in e && i(e[r]) ? n[r] = $(e[r], t[r]) : n[r] = t[r];
  }), n;
}
function w(...e) {
  return e.reduce((t, n, o) => o === 0 ? n : $(t, n), {});
}
function m(e, ...t) {
  return c(e) ? e(...t) : e;
}
function a(e, t = true) {
  return typeof e == "string" && (t || e !== "");
}
function g(e) {
  return a(e) ? e.replace(/(-|_)/g, "").toLowerCase() : e;
}
function F(e, t = "", n = {}) {
  let o = g(t).split("."), r = o.shift();
  if (r) {
    if (i(e)) {
      let u = Object.keys(e).find((f) => g(f) === r) || "";
      return F(m(e[u], n), o.join("."), n);
    }
    return;
  }
  return m(e, n);
}
function C(e, t = true) {
  return Array.isArray(e) && (t || e.length !== 0);
}
function z(e) {
  return s2(e) && !isNaN(e);
}
function G(e, t) {
  if (t) {
    let n = t.test(e);
    return t.lastIndex = 0, n;
  }
  return false;
}
function H(...e) {
  return w(...e);
}
function Y(e) {
  return e && e.replace(/\/\*(?:(?!\*\/)[\s\S])*\*\/|[\r\n\t]+/g, "").replace(/ {2,}/g, " ").replace(/ ([{:}]) /g, "$1").replace(/([;,]) /g, "$1").replace(/ !/g, "!").replace(/: /g, ":").trim();
}
function re(e) {
  return a(e) ? e.replace(/(_)/g, "-").replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase() : e;
}

// node_modules/@primeuix/utils/dist/dom/index.mjs
function y(t) {
  if (t) {
    let e = t.parentNode;
    return e && e instanceof ShadowRoot && e.host && (e = e.host), e;
  }
  return null;
}
function T(t) {
  return !!(t !== null && typeof t != "undefined" && t.nodeName && y(t));
}
function c2(t) {
  return typeof Element != "undefined" ? t instanceof Element : t !== null && typeof t == "object" && t.nodeType === 1 && typeof t.nodeName == "string";
}
function A(t, e = {}) {
  if (c2(t)) {
    let o = (n, r) => {
      var l2, d2;
      let i2 = (l2 = t == null ? void 0 : t.$attrs) != null && l2[n] ? [(d2 = t == null ? void 0 : t.$attrs) == null ? void 0 : d2[n]] : [];
      return [r].flat().reduce((s3, a2) => {
        if (a2 != null) {
          let u = typeof a2;
          if (u === "string" || u === "number") s3.push(a2);
          else if (u === "object") {
            let p = Array.isArray(a2) ? o(n, a2) : Object.entries(a2).map(([f, g3]) => n === "style" && (g3 || g3 === 0) ? `${f.replace(/([a-z])([A-Z])/g, "$1-$2").toLowerCase()}:${g3}` : g3 ? f : void 0);
            s3 = p.length ? s3.concat(p.filter((f) => !!f)) : s3;
          }
        }
        return s3;
      }, i2);
    };
    Object.entries(e).forEach(([n, r]) => {
      if (r != null) {
        let i2 = n.match(/^on(.+)/);
        i2 ? t.addEventListener(i2[1].toLowerCase(), r) : n === "p-bind" || n === "pBind" ? A(t, r) : (r = n === "class" ? [...new Set(o("class", r))].join(" ").trim() : n === "style" ? o("style", r).join(";").trim() : r, (t.$attrs = t.$attrs || {}) && (t.$attrs[n] = r), t.setAttribute(n, r));
      }
    });
  }
}
function q(t, e = {}) {
  return t ? `<style${Object.entries(e).reduce((o, [n, r]) => o + ` ${n}="${r}"`, "")}>${t}</style>` : "";
}
function tt() {
  return !!(typeof window != "undefined" && window.document && window.document.createElement);
}
function _t(t, e = "", o) {
  c2(t) && o !== null && o !== void 0 && t.setAttribute(e, o);
}

// node_modules/@primeuix/utils/dist/zindex/index.mjs
function g2() {
  let r = [], i2 = (e, n, t = 999) => {
    let s3 = u(e, n, t), o = s3.value + (s3.key === e ? 0 : t) + 1;
    return r.push({ key: e, value: o }), o;
  }, d2 = (e) => {
    r = r.filter((n) => n.value !== e);
  }, a2 = (e, n) => u(e, n).value, u = (e, n, t = 0) => [...r].reverse().find((s3) => n ? true : s3.key === e) || { key: e, value: t }, l2 = (e) => e && parseInt(e.style.zIndex, 10) || 0;
  return { get: l2, set: (e, n, t) => {
    n && (n.style.zIndex = String(i2(e, true, t)));
  }, clear: (e) => {
    e && (d2(l2(e)), e.style.zIndex = "");
  }, getCurrent: (e) => a2(e, true) };
}
var x = g2();

// node_modules/@primeuix/styled/dist/index.mjs
var rt = Object.defineProperty;
var st = Object.defineProperties;
var nt = Object.getOwnPropertyDescriptors;
var F2 = Object.getOwnPropertySymbols;
var xe = Object.prototype.hasOwnProperty;
var be = Object.prototype.propertyIsEnumerable;
var _e = (e, t, r) => t in e ? rt(e, t, { enumerable: true, configurable: true, writable: true, value: r }) : e[t] = r;
var h = (e, t) => {
  for (var r in t || (t = {})) xe.call(t, r) && _e(e, r, t[r]);
  if (F2) for (var r of F2(t)) be.call(t, r) && _e(e, r, t[r]);
  return e;
};
var $2 = (e, t) => st(e, nt(t));
var v = (e, t) => {
  var r = {};
  for (var s3 in e) xe.call(e, s3) && t.indexOf(s3) < 0 && (r[s3] = e[s3]);
  if (e != null && F2) for (var s3 of F2(e)) t.indexOf(s3) < 0 && be.call(e, s3) && (r[s3] = e[s3]);
  return r;
};
function ke(...e) {
  return w(...e);
}
var at = s();
var N2 = at;
var k = /{([^}]*)}/g;
var ne = /(\d+\s+[\+\-\*\/]\s+\d+)/g;
var ie2 = /var\([^)]+\)/g;
function oe(e) {
  return a(e) ? e.replace(/[A-Z]/g, (t, r) => r === 0 ? t : "." + t.toLowerCase()).toLowerCase() : e;
}
function Lt(e, t) {
  C(e) ? e.push(...t || []) : i(e) && Object.assign(e, t);
}
function ve(e) {
  return i(e) && e.hasOwnProperty("$value") && e.hasOwnProperty("$type") ? e.$value : e;
}
function At(e, t = "") {
  return ["opacity", "z-index", "line-height", "font-weight", "flex", "flex-grow", "flex-shrink", "order"].some((s3) => t.endsWith(s3)) ? e : `${e}`.trim().split(" ").map((a2) => z(a2) ? `${a2}px` : a2).join(" ");
}
function dt(e) {
  return e.replaceAll(/ /g, "").replace(/[^\w]/g, "-");
}
function Q(e = "", t = "") {
  return dt(`${a(e, false) && a(t, false) ? `${e}-` : e}${t}`);
}
function ae2(e = "", t = "") {
  return `--${Q(e, t)}`;
}
function ht(e = "") {
  let t = (e.match(/{/g) || []).length, r = (e.match(/}/g) || []).length;
  return (t + r) % 2 !== 0;
}
function Y2(e, t = "", r = "", s3 = [], i2) {
  if (a(e)) {
    let a2 = e.trim();
    if (ht(a2)) return;
    if (G(a2, k)) {
      let n = a2.replaceAll(k, (l2) => {
        let c3 = l2.replace(/{|}/g, "").split(".").filter((m2) => !s3.some((d2) => G(m2, d2)));
        return `var(${ae2(r, re(c3.join("-")))}${s2(i2) ? `, ${i2}` : ""})`;
      });
      return G(n.replace(ie2, "0"), ne) ? `calc(${n})` : n;
    }
    return a2;
  } else if (z(e)) return e;
}
function Dt(e = {}, t) {
  if (a(t)) {
    let r = t.trim();
    return G(r, k) ? r.replaceAll(k, (s3) => F(e, s3.replace(/{|}/g, ""))) : r;
  } else if (z(t)) return t;
}
function Re(e, t, r) {
  a(t, false) && e.push(`${t}:${r};`);
}
function C2(e, t) {
  return e ? `${e}{${t}}` : "";
}
function le(e, t) {
  if (e.indexOf("dt(") === -1) return e;
  function r(n, l2) {
    let o = [], c3 = 0, m2 = "", d2 = null, u = 0;
    for (; c3 <= n.length; ) {
      let g3 = n[c3];
      if ((g3 === '"' || g3 === "'" || g3 === "`") && n[c3 - 1] !== "\\" && (d2 = d2 === g3 ? null : g3), !d2 && (g3 === "(" && u++, g3 === ")" && u--, (g3 === "," || c3 === n.length) && u === 0)) {
        let f = m2.trim();
        f.startsWith("dt(") ? o.push(le(f, l2)) : o.push(s3(f)), m2 = "", c3++;
        continue;
      }
      g3 !== void 0 && (m2 += g3), c3++;
    }
    return o;
  }
  function s3(n) {
    let l2 = n[0];
    if ((l2 === '"' || l2 === "'" || l2 === "`") && n[n.length - 1] === l2) return n.slice(1, -1);
    let o = Number(n);
    return isNaN(o) ? n : o;
  }
  let i2 = [], a2 = [];
  for (let n = 0; n < e.length; n++) if (e[n] === "d" && e.slice(n, n + 3) === "dt(") a2.push(n), n += 2;
  else if (e[n] === ")" && a2.length > 0) {
    let l2 = a2.pop();
    a2.length === 0 && i2.push([l2, n]);
  }
  if (!i2.length) return e;
  for (let n = i2.length - 1; n >= 0; n--) {
    let [l2, o] = i2[n], c3 = e.slice(l2 + 3, o), m2 = r(c3, t), d2 = t(...m2);
    e = e.slice(0, l2) + d2 + e.slice(o + 1);
  }
  return e;
}
function Te(e) {
  return e.length === 4 ? `#${e[1]}${e[1]}${e[2]}${e[2]}${e[3]}${e[3]}` : e;
}
function Ne(e) {
  let t = parseInt(e.substring(1), 16), r = t >> 16 & 255, s3 = t >> 8 & 255, i2 = t & 255;
  return { r, g: s3, b: i2 };
}
function gt(e, t, r) {
  return `#${e.toString(16).padStart(2, "0")}${t.toString(16).padStart(2, "0")}${r.toString(16).padStart(2, "0")}`;
}
var D = (e, t, r) => {
  e = Te(e), t = Te(t);
  let a2 = (r / 100 * 2 - 1 + 1) / 2, n = 1 - a2, l2 = Ne(e), o = Ne(t), c3 = Math.round(l2.r * a2 + o.r * n), m2 = Math.round(l2.g * a2 + o.g * n), d2 = Math.round(l2.b * a2 + o.b * n);
  return gt(c3, m2, d2);
};
var ce = (e, t) => D("#000000", e, t);
var me = (e, t) => D("#ffffff", e, t);
var Ce = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950];
var ft = (e) => {
  if (G(e, k)) {
    let t = e.replace(/{|}/g, "");
    return Ce.reduce((r, s3) => (r[s3] = `{${t}.${s3}}`, r), {});
  }
  return typeof e == "string" ? Ce.reduce((t, r, s3) => (t[r] = s3 <= 5 ? me(e, (5 - s3) * 19) : ce(e, (s3 - 5) * 15), t), {}) : e;
};
var rr = (e) => {
  var a2;
  let t = S.getTheme(), r = ue(t, e, void 0, "variable"), s3 = (a2 = r == null ? void 0 : r.match(/--[\w-]+/g)) == null ? void 0 : a2[0], i2 = ue(t, e, void 0, "value");
  return { name: s3, variable: r, value: i2 };
};
var E = (...e) => ue(S.getTheme(), ...e);
var ue = (e = {}, t, r, s3) => {
  if (t) {
    let { variable: i2, options: a2 } = S.defaults || {}, { prefix: n, transform: l2 } = (e == null ? void 0 : e.options) || a2 || {}, o = G(t, k) ? t : `{${t}}`;
    return s3 === "value" || l(s3) && l2 === "strict" ? S.getTokenValue(t) : Y2(o, void 0, n, [i2.excludedKeyRegex], r);
  }
  return "";
};
function ar(e, ...t) {
  if (e instanceof Array) {
    let r = e.reduce((s3, i2, a2) => {
      var n;
      return s3 + i2 + ((n = m(t[a2], { dt: E })) != null ? n : "");
    }, "");
    return le(r, E);
  }
  return m(e, { dt: E });
}
var w2 = (e = {}) => {
  let { preset: t, options: r } = e;
  return { preset(s3) {
    return t = t ? H(t, s3) : s3, this;
  }, options(s3) {
    return r = r ? h(h({}, r), s3) : s3, this;
  }, primaryPalette(s3) {
    let { semantic: i2 } = t || {};
    return t = $2(h({}, t), { semantic: $2(h({}, i2), { primary: s3 }) }), this;
  }, surfacePalette(s3) {
    var o, c3;
    let { semantic: i2 } = t || {}, a2 = s3 && Object.hasOwn(s3, "light") ? s3.light : s3, n = s3 && Object.hasOwn(s3, "dark") ? s3.dark : s3, l2 = { colorScheme: { light: h(h({}, (o = i2 == null ? void 0 : i2.colorScheme) == null ? void 0 : o.light), !!a2 && { surface: a2 }), dark: h(h({}, (c3 = i2 == null ? void 0 : i2.colorScheme) == null ? void 0 : c3.dark), !!n && { surface: n }) } };
    return t = $2(h({}, t), { semantic: h(h({}, i2), l2) }), this;
  }, define({ useDefaultPreset: s3 = false, useDefaultOptions: i2 = false } = {}) {
    return { preset: s3 ? S.getPreset() : t, options: i2 ? S.getOptions() : r };
  }, update({ mergePresets: s3 = true, mergeOptions: i2 = true } = {}) {
    let a2 = { preset: s3 ? H(S.getPreset(), t) : t, options: i2 ? h(h({}, S.getOptions()), r) : r };
    return S.setTheme(a2), a2;
  }, use(s3) {
    let i2 = this.define(s3);
    return S.setTheme(i2), i2;
  } };
};
function de(e, t = {}) {
  let r = S.defaults.variable, { prefix: s3 = r.prefix, selector: i2 = r.selector, excludedKeyRegex: a2 = r.excludedKeyRegex } = t, n = [], l2 = [], o = [{ node: e, path: s3 }];
  for (; o.length; ) {
    let { node: m2, path: d2 } = o.pop();
    for (let u in m2) {
      let g3 = m2[u], f = ve(g3), p = G(u, a2) ? Q(d2) : Q(d2, re(u));
      if (i(f)) o.push({ node: f, path: p });
      else {
        let y2 = ae2(p), R = Y2(f, p, s3, [a2]);
        Re(l2, y2, R);
        let T2 = p;
        s3 && T2.startsWith(s3 + "-") && (T2 = T2.slice(s3.length + 1)), n.push(T2.replace(/-/g, "."));
      }
    }
  }
  let c3 = l2.join("");
  return { value: l2, tokens: n, declarations: c3, css: C2(i2, c3) };
}
var b = { regex: { rules: { class: { pattern: /^\.([a-zA-Z][\w-]*)$/, resolve(e) {
  return { type: "class", selector: e, matched: this.pattern.test(e.trim()) };
} }, attr: { pattern: /^\[(.*)\]$/, resolve(e) {
  return { type: "attr", selector: `:root${e},:host${e}`, matched: this.pattern.test(e.trim()) };
} }, media: { pattern: /^@media (.*)$/, resolve(e) {
  return { type: "media", selector: e, matched: this.pattern.test(e.trim()) };
} }, system: { pattern: /^system$/, resolve(e) {
  return { type: "system", selector: "@media (prefers-color-scheme: dark)", matched: this.pattern.test(e.trim()) };
} }, custom: { resolve(e) {
  return { type: "custom", selector: e, matched: true };
} } }, resolve(e) {
  let t = Object.keys(this.rules).filter((r) => r !== "custom").map((r) => this.rules[r]);
  return [e].flat().map((r) => {
    var s3;
    return (s3 = t.map((i2) => i2.resolve(r)).find((i2) => i2.matched)) != null ? s3 : this.rules.custom.resolve(r);
  });
} }, _toVariables(e, t) {
  return de(e, { prefix: t == null ? void 0 : t.prefix });
}, getCommon({ name: e = "", theme: t = {}, params: r, set: s3, defaults: i2 }) {
  var R, T2, j, O, M, z2, V;
  let { preset: a2, options: n } = t, l2, o, c3, m2, d2, u, g3;
  if (s2(a2) && n.transform !== "strict") {
    let { primitive: L, semantic: te, extend: re2 } = a2, f = te || {}, { colorScheme: K2 } = f, A2 = v(f, ["colorScheme"]), x2 = re2 || {}, { colorScheme: X } = x2, G2 = v(x2, ["colorScheme"]), p = K2 || {}, { dark: U } = p, B = v(p, ["dark"]), y2 = X || {}, { dark: I } = y2, H2 = v(y2, ["dark"]), W = s2(L) ? this._toVariables({ primitive: L }, n) : {}, q2 = s2(A2) ? this._toVariables({ semantic: A2 }, n) : {}, Z = s2(B) ? this._toVariables({ light: B }, n) : {}, pe = s2(U) ? this._toVariables({ dark: U }, n) : {}, fe = s2(G2) ? this._toVariables({ semantic: G2 }, n) : {}, ye = s2(H2) ? this._toVariables({ light: H2 }, n) : {}, Se = s2(I) ? this._toVariables({ dark: I }, n) : {}, [Me, ze] = [(R = W.declarations) != null ? R : "", W.tokens], [Ke, Xe] = [(T2 = q2.declarations) != null ? T2 : "", q2.tokens || []], [Ge, Ue] = [(j = Z.declarations) != null ? j : "", Z.tokens || []], [Be, Ie] = [(O = pe.declarations) != null ? O : "", pe.tokens || []], [He, We] = [(M = fe.declarations) != null ? M : "", fe.tokens || []], [qe, Ze] = [(z2 = ye.declarations) != null ? z2 : "", ye.tokens || []], [Fe, Je] = [(V = Se.declarations) != null ? V : "", Se.tokens || []];
    l2 = this.transformCSS(e, Me, "light", "variable", n, s3, i2), o = ze;
    let Qe = this.transformCSS(e, `${Ke}${Ge}`, "light", "variable", n, s3, i2), Ye = this.transformCSS(e, `${Be}`, "dark", "variable", n, s3, i2);
    c3 = `${Qe}${Ye}`, m2 = [.../* @__PURE__ */ new Set([...Xe, ...Ue, ...Ie])];
    let et = this.transformCSS(e, `${He}${qe}color-scheme:light`, "light", "variable", n, s3, i2), tt2 = this.transformCSS(e, `${Fe}color-scheme:dark`, "dark", "variable", n, s3, i2);
    d2 = `${et}${tt2}`, u = [.../* @__PURE__ */ new Set([...We, ...Ze, ...Je])], g3 = m(a2.css, { dt: E });
  }
  return { primitive: { css: l2, tokens: o }, semantic: { css: c3, tokens: m2 }, global: { css: d2, tokens: u }, style: g3 };
}, getPreset({ name: e = "", preset: t = {}, options: r, params: s3, set: i2, defaults: a2, selector: n }) {
  var f, x2, p;
  let l2, o, c3;
  if (s2(t) && r.transform !== "strict") {
    let y2 = e.replace("-directive", ""), m2 = t, { colorScheme: R, extend: T2, css: j } = m2, O = v(m2, ["colorScheme", "extend", "css"]), d2 = T2 || {}, { colorScheme: M } = d2, z2 = v(d2, ["colorScheme"]), u = R || {}, { dark: V } = u, L = v(u, ["dark"]), g3 = M || {}, { dark: te } = g3, re2 = v(g3, ["dark"]), K2 = s2(O) ? this._toVariables({ [y2]: h(h({}, O), z2) }, r) : {}, A2 = s2(L) ? this._toVariables({ [y2]: h(h({}, L), re2) }, r) : {}, X = s2(V) ? this._toVariables({ [y2]: h(h({}, V), te) }, r) : {}, [G2, U] = [(f = K2.declarations) != null ? f : "", K2.tokens || []], [B, I] = [(x2 = A2.declarations) != null ? x2 : "", A2.tokens || []], [H2, W] = [(p = X.declarations) != null ? p : "", X.tokens || []], q2 = this.transformCSS(y2, `${G2}${B}`, "light", "variable", r, i2, a2, n), Z = this.transformCSS(y2, H2, "dark", "variable", r, i2, a2, n);
    l2 = `${q2}${Z}`, o = [.../* @__PURE__ */ new Set([...U, ...I, ...W])], c3 = m(j, { dt: E });
  }
  return { css: l2, tokens: o, style: c3 };
}, getPresetC({ name: e = "", theme: t = {}, params: r, set: s3, defaults: i2 }) {
  var o;
  let { preset: a2, options: n } = t, l2 = (o = a2 == null ? void 0 : a2.components) == null ? void 0 : o[e];
  return this.getPreset({ name: e, preset: l2, options: n, params: r, set: s3, defaults: i2 });
}, getPresetD({ name: e = "", theme: t = {}, params: r, set: s3, defaults: i2 }) {
  var c3, m2;
  let a2 = e.replace("-directive", ""), { preset: n, options: l2 } = t, o = ((c3 = n == null ? void 0 : n.components) == null ? void 0 : c3[a2]) || ((m2 = n == null ? void 0 : n.directives) == null ? void 0 : m2[a2]);
  return this.getPreset({ name: a2, preset: o, options: l2, params: r, set: s3, defaults: i2 });
}, applyDarkColorScheme(e) {
  return !(e.darkModeSelector === "none" || e.darkModeSelector === false);
}, getColorSchemeOption(e, t) {
  var r;
  return this.applyDarkColorScheme(e) ? this.regex.resolve(e.darkModeSelector === true ? t.options.darkModeSelector : (r = e.darkModeSelector) != null ? r : t.options.darkModeSelector) : [];
}, getLayerOrder(e, t = {}, r, s3) {
  let { cssLayer: i2 } = t;
  return i2 ? `@layer ${m(i2.order || i2.name || "primeui", r)}` : "";
}, getCommonStyleSheet({ name: e = "", theme: t = {}, params: r, props: s3 = {}, set: i2, defaults: a2 }) {
  let n = this.getCommon({ name: e, theme: t, params: r, set: i2, defaults: a2 }), l2 = Object.entries(s3).reduce((o, [c3, m2]) => o.push(`${c3}="${m2}"`) && o, []).join(" ");
  return Object.entries(n || {}).reduce((o, [c3, m2]) => {
    if (i(m2) && Object.hasOwn(m2, "css")) {
      let d2 = Y(m2.css), u = `${c3}-variables`;
      o.push(`<style type="text/css" data-primevue-style-id="${u}" ${l2}>${d2}</style>`);
    }
    return o;
  }, []).join("");
}, getStyleSheet({ name: e = "", theme: t = {}, params: r, props: s3 = {}, set: i2, defaults: a2 }) {
  var c3;
  let n = { name: e, theme: t, params: r, set: i2, defaults: a2 }, l2 = (c3 = e.includes("-directive") ? this.getPresetD(n) : this.getPresetC(n)) == null ? void 0 : c3.css, o = Object.entries(s3).reduce((m2, [d2, u]) => m2.push(`${d2}="${u}"`) && m2, []).join(" ");
  return l2 ? `<style type="text/css" data-primevue-style-id="${e}-variables" ${o}>${Y(l2)}</style>` : "";
}, createTokens(e = {}, t, r = "", s3 = "", i2 = {}) {
  let a2 = function(l2, o = {}, c3 = []) {
    if (c3.includes(this.path)) return console.warn(`Circular reference detected at ${this.path}`), { colorScheme: l2, path: this.path, paths: o, value: void 0 };
    c3.push(this.path), o.name = this.path, o.binding || (o.binding = {});
    let m2 = this.value;
    if (typeof this.value == "string" && k.test(this.value)) {
      let u = this.value.trim().replace(k, (g3) => {
        var y2;
        let f = g3.slice(1, -1), x2 = this.tokens[f];
        if (!x2) return console.warn(`Token not found for path: ${f}`), "__UNRESOLVED__";
        let p = x2.computed(l2, o, c3);
        return Array.isArray(p) && p.length === 2 ? `light-dark(${p[0].value},${p[1].value})` : (y2 = p == null ? void 0 : p.value) != null ? y2 : "__UNRESOLVED__";
      });
      m2 = ne.test(u.replace(ie2, "0")) ? `calc(${u})` : u;
    }
    return l(o.binding) && delete o.binding, c3.pop(), { colorScheme: l2, path: this.path, paths: o, value: m2.includes("__UNRESOLVED__") ? void 0 : m2 };
  }, n = (l2, o, c3) => {
    Object.entries(l2).forEach(([m2, d2]) => {
      let u = G(m2, t.variable.excludedKeyRegex) ? o : o ? `${o}.${oe(m2)}` : oe(m2), g3 = c3 ? `${c3}.${m2}` : m2;
      i(d2) ? n(d2, u, g3) : (i2[u] || (i2[u] = { paths: [], computed: (f, x2 = {}, p = []) => {
        if (i2[u].paths.length === 1) return i2[u].paths[0].computed(i2[u].paths[0].scheme, x2.binding, p);
        if (f && f !== "none") for (let y2 = 0; y2 < i2[u].paths.length; y2++) {
          let R = i2[u].paths[y2];
          if (R.scheme === f) return R.computed(f, x2.binding, p);
        }
        return i2[u].paths.map((y2) => y2.computed(y2.scheme, x2[y2.scheme], p));
      } }), i2[u].paths.push({ path: g3, value: d2, scheme: g3.includes("colorScheme.light") ? "light" : g3.includes("colorScheme.dark") ? "dark" : "none", computed: a2, tokens: i2 }));
    });
  };
  return n(e, r, s3), i2;
}, getTokenValue(e, t, r) {
  var l2;
  let i2 = ((o) => o.split(".").filter((m2) => !G(m2.toLowerCase(), r.variable.excludedKeyRegex)).join("."))(t), a2 = t.includes("colorScheme.light") ? "light" : t.includes("colorScheme.dark") ? "dark" : void 0, n = [(l2 = e[i2]) == null ? void 0 : l2.computed(a2)].flat().filter((o) => o);
  return n.length === 1 ? n[0].value : n.reduce((o = {}, c3) => {
    let u = c3, { colorScheme: m2 } = u, d2 = v(u, ["colorScheme"]);
    return o[m2] = d2, o;
  }, void 0);
}, getSelectorRule(e, t, r, s3) {
  return r === "class" || r === "attr" ? C2(s2(t) ? `${e}${t},${e} ${t}` : e, s3) : C2(e, C2(t != null ? t : ":root,:host", s3));
}, transformCSS(e, t, r, s3, i2 = {}, a2, n, l2) {
  if (s2(t)) {
    let { cssLayer: o } = i2;
    if (s3 !== "style") {
      let c3 = this.getColorSchemeOption(i2, n);
      t = r === "dark" ? c3.reduce((m2, { type: d2, selector: u }) => (s2(u) && (m2 += u.includes("[CSS]") ? u.replace("[CSS]", t) : this.getSelectorRule(u, l2, d2, t)), m2), "") : C2(l2 != null ? l2 : ":root,:host", t);
    }
    if (o) {
      let c3 = { name: "primeui", order: "primeui" };
      i(o) && (c3.name = m(o.name, { name: e, type: s3 })), s2(c3.name) && (t = C2(`@layer ${c3.name}`, t), a2 == null || a2.layerNames(c3.name));
    }
    return t;
  }
  return "";
} };
var S = { defaults: { variable: { prefix: "p", selector: ":root,:host", excludedKeyRegex: /^(primitive|semantic|components|directives|variables|colorscheme|light|dark|common|root|states|extend|css)$/gi }, options: { prefix: "p", darkModeSelector: "system", cssLayer: false } }, _theme: void 0, _layerNames: /* @__PURE__ */ new Set(), _loadedStyleNames: /* @__PURE__ */ new Set(), _loadingStyles: /* @__PURE__ */ new Set(), _tokens: {}, update(e = {}) {
  let { theme: t } = e;
  t && (this._theme = $2(h({}, t), { options: h(h({}, this.defaults.options), t.options) }), this._tokens = b.createTokens(this.preset, this.defaults), this.clearLoadedStyleNames());
}, get theme() {
  return this._theme;
}, get preset() {
  var e;
  return ((e = this.theme) == null ? void 0 : e.preset) || {};
}, get options() {
  var e;
  return ((e = this.theme) == null ? void 0 : e.options) || {};
}, get tokens() {
  return this._tokens;
}, getTheme() {
  return this.theme;
}, setTheme(e) {
  this.update({ theme: e }), N2.emit("theme:change", e);
}, getPreset() {
  return this.preset;
}, setPreset(e) {
  this._theme = $2(h({}, this.theme), { preset: e }), this._tokens = b.createTokens(e, this.defaults), this.clearLoadedStyleNames(), N2.emit("preset:change", e), N2.emit("theme:change", this.theme);
}, getOptions() {
  return this.options;
}, setOptions(e) {
  this._theme = $2(h({}, this.theme), { options: e }), this.clearLoadedStyleNames(), N2.emit("options:change", e), N2.emit("theme:change", this.theme);
}, getLayerNames() {
  return [...this._layerNames];
}, setLayerNames(e) {
  this._layerNames.add(e);
}, getLoadedStyleNames() {
  return this._loadedStyleNames;
}, isStyleNameLoaded(e) {
  return this._loadedStyleNames.has(e);
}, setLoadedStyleName(e) {
  this._loadedStyleNames.add(e);
}, deleteLoadedStyleName(e) {
  this._loadedStyleNames.delete(e);
}, clearLoadedStyleNames() {
  this._loadedStyleNames.clear();
}, getTokenValue(e) {
  return b.getTokenValue(this.tokens, e, this.defaults);
}, getCommon(e = "", t) {
  return b.getCommon({ name: e, theme: this.theme, params: t, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } });
}, getComponent(e = "", t) {
  let r = { name: e, theme: this.theme, params: t, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } };
  return b.getPresetC(r);
}, getDirective(e = "", t) {
  let r = { name: e, theme: this.theme, params: t, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } };
  return b.getPresetD(r);
}, getCustomPreset(e = "", t, r, s3) {
  let i2 = { name: e, preset: t, options: this.options, selector: r, params: s3, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } };
  return b.getPreset(i2);
}, getLayerOrderCSS(e = "") {
  return b.getLayerOrder(e, this.options, { names: this.getLayerNames() }, this.defaults);
}, transformCSS(e = "", t, r = "style", s3) {
  return b.transformCSS(e, t, s3, r, this.options, { layerNames: this.setLayerNames.bind(this) }, this.defaults);
}, getCommonStyleSheet(e = "", t, r = {}) {
  return b.getCommonStyleSheet({ name: e, theme: this.theme, params: t, props: r, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } });
}, getStyleSheet(e, t, r = {}) {
  return b.getStyleSheet({ name: e, theme: this.theme, params: t, props: r, defaults: this.defaults, set: { layerNames: this.setLayerNames.bind(this) } });
}, onStyleMounted(e) {
  this._loadingStyles.add(e);
}, onStyleUpdated(e) {
  this._loadingStyles.add(e);
}, onStyleLoaded(e, { name: t }) {
  this._loadingStyles.size && (this._loadingStyles.delete(t), N2.emit(`theme:${t}:load`, e), !this._loadingStyles.size && N2.emit("theme:load"));
} };
function Ve(...e) {
  let t = w(S.getPreset(), ...e);
  return S.setPreset(t), t;
}
function Le(e) {
  return w2().primaryPalette(e).update().preset;
}
function Ae(e) {
  return w2().surfacePalette(e).update().preset;
}
function De(...e) {
  let t = w(...e);
  return S.setPreset(t), t;
}
function je(e) {
  return w2(e).update({ mergePresets: false });
}
var ge = class {
  constructor({ attrs: t } = {}) {
    this._styles = /* @__PURE__ */ new Map(), this._attrs = t || {};
  }
  get(t) {
    return this._styles.get(t);
  }
  has(t) {
    return this._styles.has(t);
  }
  delete(t) {
    this._styles.delete(t);
  }
  clear() {
    this._styles.clear();
  }
  add(t, r) {
    if (s2(r)) {
      let s3 = { name: t, css: r, attrs: this._attrs, markup: q(r, this._attrs) };
      this._styles.set(t, $2(h({}, s3), { element: this.createStyleElement(s3) }));
    }
  }
  update() {
  }
  getStyles() {
    return this._styles;
  }
  getAllCSS() {
    return [...this._styles.values()].map((t) => t.css).filter(String);
  }
  getAllMarkup() {
    return [...this._styles.values()].map((t) => t.markup).filter(String);
  }
  getAllElements() {
    return [...this._styles.values()].map((t) => t.element);
  }
  createStyleElement(t = {}) {
  }
};
var Nt = ge;

export {
  s2 as s,
  m,
  H,
  Y,
  T,
  A,
  tt,
  _t,
  ke,
  N2 as N,
  k,
  ne,
  ie2 as ie,
  oe,
  Lt,
  ve,
  At,
  dt,
  Q,
  ae2 as ae,
  ht,
  Y2,
  Dt,
  Re,
  C2 as C,
  le,
  D,
  ce,
  me,
  ft,
  rr,
  E,
  ue,
  ar,
  w2 as w,
  de,
  b,
  S,
  Ve,
  Le,
  Ae,
  De,
  je,
  Nt
};
//# sourceMappingURL=chunk-GSUW45W2.js.map
