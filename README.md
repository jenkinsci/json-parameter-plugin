# json-parameter

A Jenkins plugin that allows parameters to be populated dynamically using 
JSON data from configurable sources.

[![Plugin](https://img.shields.io/jenkins/plugin/v/json-parameter.svg)](https://plugins.jenkins.io/json-parameter)
[![Build](https://ci.jenkins.io/job/Plugins/job/json-parameter-plugin/job/main/badge/icon)](https://ci.jenkins.io/job/Plugins/job/json-parameter-plugin/job/main)
[![Security Scan](https://github.com/jenkinsci/json-parameter-plugin/actions/workflows/jenkins-security-scan.yaml/badge.svg)](https://github.com/jenkinsci/json-parameter-plugin/actions/workflows/jenkins-security-scan.yaml)
<!--
[![Plugin Installs](https://img.shields.io/jenkins/plugin/i/json-parameter.svg?color=blue&label=installations)](https://plugins.jenkins.io/json-parameter)
-->
---

## 🚀 Introduction

The JSON Parameter Plugin introduces a new parameter type for Jenkins jobs: JSON Parameter.
It enables jobs to dynamically fetch, parse, and populate values from JSON sources at runtime 
or configuration time.

Supported JSON sources:

- ✅ Jenkins **Config File Provider** (folder-based or global)
- ✅ **Remote HTTP** endpoints

You can use JSONPath expressions – including filters, regex, and conditions. 
See [Advanced JSONPath Examples](#-advanced-jsonpath-queries).

---

## Quick Preview

### Config File Source
<picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/config-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/config-light.png">
    <img alt="JSON Parameter: Config File Source" src="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/config-light.png">
</picture>

### Remote HTTP Source
<picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/remote-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/remote-light.png">
    <img alt="JSON Parameter: Remote HTTP Source" src="https://raw.githubusercontent.com/jenkinsci/json-parameter-plugin/main/docs/images/remote-light.png">
</picture>


---

## ⚙️ Getting Started

### 1. Install the plugin

Install via Jenkins Plugin Manager:  
**Manage Jenkins » Plugins » Available plugins » JSON Parameter**

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
    - Username/Password → Basic Auth
    - Secret Text → Bearer token

---

### 4. Examples

**📦 Sample JSON**

```json
[
  {
    "name": "Alpha"
  },
  {
    "name": "Beta"
  }
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

```json
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

```json
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
            source: remoteSource(credentialsId: 'my-id', url: 'http://localhost:8080/api/data')
    )
}
```

➡️ Rendered dropdown:

```json
["Beta", "Alpha"]
```

---

**🔧 Example 4: Reference another parameter**

Sometimes you want the available options to depend on the value of another parameter.  
Use `jsonParamRef` with a JSONPath query containing a placeholder like `${OTHER_PARAM}`.

**📦 Sample JSON**

```json
[
  { "name": "Alice", "email": "alice@example.com" },
  { "name": "Bob",   "email": "bob@example.com" }
]
```

```groovy
parameters {
    jsonParam(
            name: 'USERS',
            description: 'List of available users',
            defaultValue: 'Alice',
            query: '$[*].name',
            source: configFileSource(configId: 'users-json')
    )

    jsonParamRef(
            name: 'EMAILS',
            description: 'Email addresses filtered by selected user',
            defaultValue: '',
            query: '$[?(@.name == "${USERS}")].email',
            ref: 'USERS',
            source: configFileSource(configId: 'users-json')
    )
}
```

➡️ Rendered dropdown:

depends on the selected user, e.g.
```json
["alice@example.com"]
```

---

### 🔮 Advanced JSONPath Queries

```json
{
  "files": [
    {
      "type": "json",
      "value": "file1.json"
    },
    {
      "type": "yaml",
      "value": "file2.yaml"
    },
    {
      "type": "properties",
      "value": "file3.properties"
    },
    {
      "type": "yaml",
      "value": "file4.yml"
    },
    {
      "type": "json",
      "value": "file5.json"
    }
  ]
}
```

---

**🔧 Query 1: Get JSON files**
```groovy
'$.files[?(@.type == "json")].value'
```
➡️ Rendered dropdown:
```json
["-- Choose an option --", "file1.json", "file5.json"]
```

---

**🔧 Query 2: Get files and exclude Properties files**
```groovy
'$.files[?(@.type != "properties")].value'
```
➡️ Rendered dropdown:
```json
["-- Choose an option --", "file1.json", "file2.yaml", "file4.yml", "file5.json"]
```

---

**🔧 Query 3: Get YAML files by Regex**
```groovy
'$.files[?(@.value =~ /.*\\.ya?ml$/)].value'
```
➡️ Rendered dropdown:
```json
["-- Choose an option --", "file2.yaml", "file4.yml"]
```

---

**🔧 Query 4: Get YAML files using the contains operator**
```groovy
'$.files[?(@.value contains "yaml")].value'
```
➡️ Rendered dropdown:
```json
["-- Choose an option --", "file2.yaml"]
```

---
**🔧 Query 5: Get YAML and Properties files combined**
```groovy
'$.files[?(@.value =~ /.*\\.ya?ml$/ || @.type == "properties")].value'
```
➡️ Rendered dropdown:
```json
["-- Choose an option --", "file2.yaml", "file3.properties", "file4.yml"]
```

---

### 💡 You can also generate the DSL snippet via the Pipeline Syntax Generator in Jenkins.

---

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under the MIT [LICENSE](LICENSE.md)

