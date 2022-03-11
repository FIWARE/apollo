# Contributing to the Notification-Proxy

Thanks for checking out the Notification-Proxy. In order to contribute, please check the general [FIWARE development guidelines](https://fiware-requirements.readthedocs.io/en/latest/lifecycle/index.html).

## Coding guidelines


Your contributions should try to follow the [google java coding guidelines](https://google.github.io/styleguide/javaguide.html). The structure of your
code should fit the principles of [Domain Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html) and use the DI-mechanisms of
[Mirconaut](https://docs.micronaut.io/3.1.3/guide/index.html). Be aware of the framework and make use of its functionalities wherever it makes sense.
Additional tooling for code-generation is included in the project([lombok](https://projectlombok.org/), [openAPI-codegen](https://github.com/kokuwaio/micronaut-openapi-codegen),
[mapstruct](https://mapstruct.org/)) in order to reduce boiler-plate code.


## Pull Request

Since this project uses automatic versioning, please apply one of the following labels to your pull request:
* patch - the PR contains a fix
* minor - the PR contains a new feature/improvement
* major - the PR contains a breaking change

The PRs enforce squash merge. Please provide a proper description on your squash, it will be used for release notes.

## Vulnerabilities

Please report vulnerabilities as [bugs](#bug) or email the authors.

## Bugs & Enhancements

If you find bug or searching for a new feature, please check the [issues](https://github.com/wistefan/notification-proxy/issues) and [pull requests](https://github.com/wistefan/notification-proxy/pulls)
first.

### Bug

If your bug is not already mentioned, please create either a [PR](#pull-request) or a new issue. The issue should contain a brief description on the
observed behaviour, your expectation and a description on how to reproduce it (bonus points if you provide a testcase for reproduction;).

### Enhancement

Create an issue including a proper description for the new feature. You can also start with a PR right away, but it would be easier to align on the details
before and save unnessary work if discussed before. Don't forget to add proper tests for any new feature.