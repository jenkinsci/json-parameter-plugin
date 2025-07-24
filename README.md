# json-parameter

A Jenkins plugin that allows parameters to be populated dynamically using JSON data from configurable sources.

[![Build](https://ci.jenkins.io/job/Plugins/job/json-parameter-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/json-parameter-plugin/job/main)<br/>
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/json-parameter-plugin.svg?color=blue)](https://github.com/jenkinsci/json-parameter-plugin/graphs/contributors)<br/>
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/json-parameter.svg?color=blue&label=installations)](https://plugins.jenkins.io/json-parameter)<br/>
[![Plugin](https://img.shields.io/jenkins/plugin/v/json-parameter.svg)](https://plugins.jenkins.io/json-parameter)<br/>
[![GitHub release](https://img.shields.io/github/release/jenkinsci/json-parameter-plugin.svg?label=changelog)](https://github.com/jenkinsci/json-parameter-plugin/releases/latest)

---

## 🚀 Introduction

This plugin defines a new parameter type: **JSON Parameter**.  
It allows Jenkins jobs to dynamically fetch, parse, and populate values from JSON sources at runtime or configuration time.

Supported JSON sources:

- ✅ Jenkins **Config File Provider** (folder-based or global)
- ✅ **Remote HTTP** endpoints

You can extract values using **JSONPath** syntax, making it easy to map dynamic structures into usable parameter options.

---

## ⚙️ Getting Started

### 1. Install the plugin

Install via Jenkins Plugin Manager:  
**Manage Jenkins » Plugin Manager » Available » json-parameter**

Requires:
- [Config File Provider Plugin](https://plugins.jenkins.io/config-file-provider/)
- Jenkins version ≥ 2.361.4 recommended

### 2. Add JSON Parameter

When configuring a job:

1. Go to "Add Parameter" → "JSON Parameter"
2. Fill in the following:
    - **Name**: Internal parameter name
    - **Description** *(optional)*
    - **Default Value** *(optional)*
    - **Query**: JSONPath expression (e.g. `$[*].name`)

### 3. Choose a Source

#### 🔹 Config File
- Choose between:
    - Folder-level Config File (via `Folder Config File Property`)
    - Global Config File
- Specify `Config File ID` (and `Folder Path` if needed)

#### 🔹 HTTP Request
- Enter the full API URL returning JSON
- *(Credential support coming soon)*

### 4. Example

**Sample JSON:**

```json
[
  { "name": "Alpha" },
  { "name": "Beta" }
]
```

## Changelog

📄 [Changelog](CHANGELOG.md)

## Issues

TODO Decide where you're going to host your issues, the default is Jenkins JIRA, but you can also enable GitHub issues,
If you use GitHub issues there's no need for this section; else add the following line:

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins.io/).

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

