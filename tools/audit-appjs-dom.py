import re
import pathlib
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parents[1]
html = (ROOT / "src/main/resources/static/index.html").read_text(encoding="utf-8")
js = (ROOT / "src/main/resources/static/app.js").read_text(encoding="utf-8")

ids_html = re.findall(r'\bid="([^"]+)"', html)
dups = [element_id for element_id, count in Counter(ids_html).items() if count > 1]
print(f"duplicate html ids: {len(dups)}")
for element_id in dups[:30]:
    print("  DUP", element_id, Counter(ids_html)[element_id])

direct = re.findall(r'getElementById\("([^"]+)"\)\.addEventListener', js)
optional = set(re.findall(r'getElementById\("([^"]+)"\)\?\.addEventListener', js))
missing = []
for element_id in direct:
    if element_id in optional:
        continue
    if f'id="{element_id}"' not in html:
        missing.append(element_id)

print(f"missing direct bindings in full app.js: {len(missing)}")
for element_id in sorted(set(missing)):
    print(element_id)
