import re
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
html = (ROOT / "src/main/resources/static/index.html").read_text(encoding="utf-8")
js = (ROOT / "src/main/resources/static/app.js").read_text(encoding="utf-8")
start = js.index('document.addEventListener("DOMContentLoaded"')
end = js.index("async function initializeAuth")
block = js[start:end]

ids_direct = re.findall(r'getElementById\("([^"]+)"\)\.addEventListener', block)
ids_all = re.findall(r'getElementById\("([^"]+)"\)', block)
missing_direct = [element_id for element_id in ids_direct if f'id="{element_id}"' not in html]
missing_all = [element_id for element_id in ids_all if f'id="{element_id}"' not in html]
print(f"direct addEventListener missing: {len(missing_direct)}")
for element_id in missing_direct:
    print("  DIRECT", element_id)
print(f"all getElementById missing: {len(missing_all)}")
for element_id in missing_all:
    print("  ALL", element_id)
