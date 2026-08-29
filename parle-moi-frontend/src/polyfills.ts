// Polyfill obligatoire pour sockjs-client (utilise par le chat temps reel) :
// cette librairie s'attend a trouver la variable globale "global" heritee de Node.js,
// qui n'existe pas nativement dans le navigateur depuis qu'Angular utilise esbuild.
(window as any).global = window;