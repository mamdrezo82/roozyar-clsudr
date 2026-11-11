# Applying this drop to the existing repo

The previous skeleton was multi-module. After copying this project over the repo root, delete the old,
now-unused top-level folders so Gradle does not see stale sources:

    git rm -r core feature docs/PHASE_1_FOUNDATION.md
    # then copy this drop's files in, keep design-reference/ as is
