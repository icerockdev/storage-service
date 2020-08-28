# Storage service utils
- S3 storage wrapper (based on AWS S3 java lib)
- Preview tools for generation, store and delete

## Installation
````kotlin
// Append repository
repositories {
    maven { url = url("https://dl.bintray.com/icerockdev/backend") }
}

// Append dependency
implementation("com.icerockdev:storage-service:0.2.1")
````

## Library usage
Lib include tools for:
 - s3 interface
    - put object to bucket
    - delete the object from bucket
    - list by prefix in bucket
    - create bucket
    - delete bucket
    - copy object (including between buckets)
    - delete bucket with content
    - samples on test and sample http server 
 - Preview
    - PreviewService 
        - generatePath 
        - generate and store preview
        - delete preview
        - allow a configuration for storage and src/dst buckets
    - PreviewConfig for project previews configuration
    - AbstractPreview and implementations for process images configuration and generate special names for storing 
    - samples on test 

## TODO
 - Support for local file system by interface
 - More preview helpers (see https://github.com/sksamuel/scrimage)
 - Optimization for coroutines
 - Tools for sending resize task to queue
 - Other media tools (like video processing)
    
## Contributing
All development (both new features and bug fixes) is performed in the `develop` branch. This way `master` always contains the sources of the most recently released version. Please send PRs with bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` on release.

For more details on contributing please see the [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2020 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
