import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/generated/jar-resources/fcEnhancedTabs/fc-enhanced-tabs.css?inline';
import $cssFromFile_1 from 'Frontend/generated/jar-resources/fcEnhancedTabs/fc-enhanced-tabs-legacy.css?inline';
import $cssFromFile_2 from 'Frontend/generated/jar-resources/fcEnhancedTabs/vaadin-menu-bar-button-legacy.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/popover/theme/lumo/vaadin-popover.js';
import 'Frontend/generated/jar-resources/vaadin-popover/popover.ts';
import 'Frontend/generated/jar-resources/font-awesome-iron-iconset/fas.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/progress-bar/theme/lumo/vaadin-progress-bar.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import 'Frontend/generated/jar-resources/font-awesome-iron-iconset/fab.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

injectGlobalCss($cssFromFile_0.toString(), 'CSSImport end', document);

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);
const $css_2 = typeof $cssFromFile_2  === 'string' ? unsafeCSS($cssFromFile_2) : $cssFromFile_2;
registerStyles('vaadin-menu-bar-button', $css_2, {moduleId: 'flow_css_mod_2'});

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '5fa9642f042261fd196bd1dd7f641f11ed894986db8c596fd68edb11682300e2') {
    pending.push(import('./chunks/chunk-3610d3c38c579b45c8ac6eceac83df189d97dd3e335cedb40693105114cc02a9.js'));
  }
  if (key === '468b0d95c591b79724c926bf9e164b875ce25eedeec185f869c36e3896bfd0bb') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '4c885914454ab8effa1d5d281fce04b8f522493d0eaa936d58c5001a111de250') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === 'd77d347236242b924d6ab835f3e4a1c3342a03b4e39b863df7f3a23c0c600711') {
    pending.push(import('./chunks/chunk-f93339bb19b6b5f17c3c828da3fef1ca0d5772ce23d0ab29c3ff53a4e4e501af.js'));
  }
  if (key === '025ddf84e213cb5b9c0c245bc21ccaa9a232705d3aac64abefe61cf4d33350d2') {
    pending.push(import('./chunks/chunk-797a7ae1c7397ba9189b057129c3df03dcace1fdfadf7be9b86ca9d69e71598e.js'));
  }
  if (key === '3075f782bc4e1f09613960633e1e134f37e61e0f9de6594dcda347ed327ce4c4') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === 'd64b07cad512d6f31faf6c8bd07d4697edccaa1654bb80eaa6d5a2d9e05ad658') {
    pending.push(import('./chunks/chunk-627d30960cc051659130ff5e8dc2be9cccfb2708d521a5d9e34a5c386bc061a7.js'));
  }
  if (key === '79fc8f02cb5ca5737aa15326808e638ae58e0ae6545d101ef64524a8e6c15ff1') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '839767dbe1ca31b9288ce28c00141d25a981f892b00768f0bdc8cf1ad52aefa8') {
    pending.push(import('./chunks/chunk-ea2e46dd5d389d49d96a69ac9e66d31da56b033cabd40cd37f2657200b0c37a0.js'));
  }
  if (key === '332b41ad5b0e62a681d65958afabdc9fb286488152508a3f5e2febc5889c57cb') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '4433a6722416343b924ab93ec67fcb97fb46d6eee844449608368e0a01ba65a8') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '9b24671beeeb7e185d92860f441c215288705a3bdebce4f31f95ff257839a190') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '202aac5b5d529ba267573fd42737359d64a4278c3deffede5dd9b7f1ca1ea48d') {
    pending.push(import('./chunks/chunk-61bdd4acf6eeadb413424979eda382bc3263f7b121164f50d32f1d6767ebf9da.js'));
  }
  if (key === '6ea20b8f0339a1c141f2a28f3741eab050daeed59c169c16eda121e7337a380b') {
    pending.push(import('./chunks/chunk-3610d3c38c579b45c8ac6eceac83df189d97dd3e335cedb40693105114cc02a9.js'));
  }
  if (key === 'f50920e3edd20173f7db6172f2fab0ffedd1972e20e72b43ae345037698cf7bc') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '848dc4222e8e19417ec12375a6593d69bffaccad179baec2a06c8170554b035a') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === '19c200b392673a7d6dbb3141e77bf2b6ed67fadfa07dd8e020cd47ee5acae17a') {
    pending.push(import('./chunks/chunk-ee912b2b46c210e53ef85b7f0a1d81f29de75d43b99fa45998db5ff7459f54bd.js'));
  }
  if (key === 'd12729a6930debb74e754c64b55d93cf673f287e3da8d6910f231a562256e60e') {
    pending.push(import('./chunks/chunk-3dc1344b1f54dbf451dbf3217dfa8f7f02bd8091e28e2b8a64f1138834dd3268.js'));
  }
  if (key === 'c9c467414167bb9c33d062bbb2e281de499cadb04923f7740e2a342fe232b7a4') {
    pending.push(import('./chunks/chunk-215bc46c1a3c5b53b0b773fd98130285ac9c364691809bd616f0c9ee7b16d922.js'));
  }
  if (key === 'a7520453d0c7f28686d8ce2c2bce90ec51771aa1e10c7d3b09bfb1969d68ee54') {
    pending.push(import('./chunks/chunk-3610d3c38c579b45c8ac6eceac83df189d97dd3e335cedb40693105114cc02a9.js'));
  }
  if (key === '91592b0f15dcba5fe3cc8f939cf856f55da0f5d326d0cecadc57588eb55ecc42') {
    pending.push(import('./chunks/chunk-14752ee127bb16f01874322d0e36b8cbe6e58620bd6f8bb3eddfb6fc7b389e84.js'));
  }
  if (key === 'f76ba4ce73c1459a65a68b302242e501b5871b8322ca93fb8f36863bf0bc569e') {
    pending.push(import('./chunks/chunk-3610d3c38c579b45c8ac6eceac83df189d97dd3e335cedb40693105114cc02a9.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}