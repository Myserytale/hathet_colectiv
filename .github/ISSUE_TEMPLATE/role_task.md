---
name: Role Task / Feature Issue
about: Assign a sprint task to one of the 9 team roles (Backend, Frontend, DB, QA, UI, BA, Architect, Tech Writer, PM)
title: "[ISS-XX] [Role] Short task title"
labels: ["task"]
assignees: []
---

## 1. Role & Sprint
- **Assigned Role:** `Backend / Frontend / Database / QA / UI Designer / Business Analyst / Technical Writer`
- **Target Sprint:** `Sprint 1 / Sprint 2 / Sprint 3 / Sprint 4`
- **Story Points:** `3`

## 2. Target Files to Modify in Pull Request
- `path/to/file`

## 3. Description & Architectural Context
Describe what needs to be implemented based on `docs/architecture/ARCHITECTURE.md` and `docs/pm/TEAM_ROLES_AND_PLANS.md`.

## 4. Acceptance Criteria (Definition of Done)
- [ ] Replaces the `TODO (ISS-XX)` stub in the target file(s)
- [ ] Passes `./scripts/verify_skeleton.sh` and GitHub Actions CI
- [ ] Reviewed and approved by Project Manager / Software Architect
