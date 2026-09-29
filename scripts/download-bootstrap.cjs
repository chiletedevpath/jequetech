const fs = require("node:fs");
(async () => {
  for (const [url, path] of [
    [
      "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css",
      "src/main/resources/static/vendor/bootstrap.min.css",
    ],
    [
      "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js",
      "src/main/resources/static/vendor/bootstrap.bundle.min.js",
    ],
    [
      "https://raw.githubusercontent.com/twbs/bootstrap/v5.3.8/LICENSE",
      "src/main/resources/static/vendor/BOOTSTRAP-LICENSE.txt",
    ],
  ]) {
    const r = await fetch(url);
    if (!r.ok) throw new Error(`${r.status} ${url}`);
    fs.writeFileSync(path, await r.text());
    console.log(path);
  }
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
