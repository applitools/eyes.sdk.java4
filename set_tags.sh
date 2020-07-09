BRANCH="tagging"

# Are we on the right branch?
if [ "$TRAVIS_BRANCH" = "$BRANCH" ]; then

  #  # Is this not a Pull Request?
  #  if [ "$TRAVIS_PULL_REQUEST" = false ]; then

  # Is this not a build which was triggered by setting a new tag?
  if [ -z "$TRAVIS_TAG" ]; then
    echo -e "Starting to tag commit.\n"
    echo -e $RELEASE_CANDIDATE

    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "Travis"

    # Add tag and push to master.
    git tag -a "${RELEASE_CANDIDATE}" -m "Travis build pushed a tag."
    git push https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG.git --tags
    git fetch origin

    echo -e "Done commit with tags.\n"
  fi
#  fi
fi
