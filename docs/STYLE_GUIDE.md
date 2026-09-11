# Visual and interaction style guide

Event Operations should feel calm, factual and useful: a working European public-sector operations tool, not a marketing site or an intelligence dashboard. It must not imply official CEPOL approval in this demonstrator.

## Product language

- Use British English (`en-GB`) in user-facing text: “authorising”, “organisation” and “sign in”.
- Use sentence case for headings, buttons, labels and status text.
- Prefer direct labels such as “Active activities”, “Add activity”, “Back to activities” and “Send nomination invitation”.
- Explain errors in terms of what failed, what remains available and how to recover.
- Do not expose stack traces, local paths, credentials or implementation details in the interface.

## Colour tokens

CSS custom properties are the single source of colour values in `frontend/src/style.css`:

| Role | Token | Value | Use |
| --- | --- | --- | --- |
| Primary | `--colour-navy-900` | `#0b1f3a` | Header, primary text and strong controls |
| Link/action | `--colour-blue-700` | `#175a9b` | Links, buttons and selected controls |
| Interactive tint | `--colour-blue-100` | `#dbeafe` | Selection and information surfaces |
| Accent | `--colour-yellow-400` | `#ffcc00` | Small highlights and focus indicators |
| Canvas | `--colour-white` | `#ffffff` | Main surfaces |
| Subtle surface | `--colour-grey-050` | `#f7f8fa` | Page canvas and quiet regions |
| Border | `--colour-grey-300` | `#c8d0da` | Dividers and control outlines |
| Muted text | `--colour-grey-700` | `#465568` | Secondary text |
| Error | `--colour-red-700` | `#a32121` | Error text and indicators |
| Success | `--colour-green-700` | `#23643b` | Confirmed states |

Yellow is an accent and must never be the only status cue. Status always includes text and/or shape. Focus indicators must remain visible on both light and dark surfaces.

## Layout and components

- Centre primary content in a readable, fluid container; do not rely on fixed heights.
- Use the spacing scale `.25rem`, `.5rem`, `.75rem`, `1rem`, `1.5rem` and `2rem`.
- Use semantic landmarks, headings and real buttons/links before adding ARIA.
- Every input has a persistent visible label and every status/error uses an appropriate live or alert role.
- Provide a skip link and visible keyboard focus. Preserve usability at 200% text size and 400% zoom.
- Reflow to one column on narrow screens. Respect `prefers-reduced-motion`.
- Keep active-activity controls before the results list in DOM and visual order.

## Dates and data

- Display known dates as `30 Jul 2026` using `Intl.DateTimeFormat('en-GB')`.
- Display unknown values as “Not supplied” or “Unknown”; do not turn missing data into a negative claim.
- Keep business codes stable and visible. Use them as entity keys and test selectors.

