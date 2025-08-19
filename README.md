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
**Manage Jenkins » Manage Jenkins » Available plugins » JSON Parameter**

Requires:
- [Config File Provider Plugin](https://plugins.jenkins.io/config-file-provider/)

### 2. Add a JSON Parameter

When configuring a job:

1. Click **"Add Parameter"** → **"JSON Parameter"**
2. Fill in the following fields:
    - **Name**: Internal parameter identifier
    - **Description** *(optional)*
    - **Default Value** *(optional)*
    - **Query**: JSONPath expression (e.g., `$[*].name`)

---

### 3. Select a JSON Source

#### 🔹 Config File
- Provide the **Config File ID**
- Jenkins resolves it hierarchically:
    - Looks in the current folder and its parents
    - Falls back to global if not found

#### 🔹 Remote HTTP Endpoint
- Enter a full API URL that returns JSON
- Select a **Credentials ID** if authentication is required:
    - Username/Password → Basic Auth (or Bearer if username is empty)
    - Secret Text → Bearer token

---

### 4. Examples

**📦 Sample JSON**

```json
[
  { "name": "Alpha" },
  { "name": "Beta" }
]
```

**🔧 Example 1: Folder-level config with placeholder**
```groovy
parameters {
  jsonParam(
          name: 'JSON_PARAM', 
          description: 'List data from JSON source.', 
          defaultValue: '', 
          query: '$[*].name', 
          source: configFileSource(configId: 'my-id')
  )
}
```
➡️ Rendered dropdown:
```groovy
["-- Choose an option --", "Alpha", "Beta"]
```

---

**🔧 Example 2: Global config with preselected default**
```groovy
parameters {
  jsonParam(
          name: 'JSON_PARAM', 
          description: 'List data from JSON source.', 
          defaultValue: 'Alpha', 
          query: '$[*].name', 
          source: configFileSource(configId: 'my-id')
  )
}
```
➡️ Rendered dropdown:
```groovy
["Alpha", "Beta"]
```

---

**🔧 Example 3: HTTP JSON source**
```groovy
parameters {
  jsonParam(
          name: 'JSON_PARAM', 
          description: 'List data from JSON source.', 
          defaultValue: 'Beta', 
          query: '$[*].name',
          source: remoteSource(credentialsId: 'my-id', url: 'https://dummyjson.com/api/data')
  )
}
```
➡️ Rendered dropdown:
```groovy
["Beta", "Alpha"]
```

---

### 💡 You can also generate the DSL snippet via the Pipeline Syntax Generator in Jenkins.

---

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

