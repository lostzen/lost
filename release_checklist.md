# Release Checklist

Instructions to build a new release version of **LOST** and deploy to [Maven Central][3].

### 0. Build & Deploy Release

Ensure that you have set a CIRCLE_TOKEN environment variable.

Run the deploy script with the release and new version specified like so:
```bash
$ ./scripts/deploy.sh 1.1.0 1.1.2-SNAPSHOT
```

Verify snapshot build was successfully deployed to [Sonatype OSS Snapshots][1].

### 1. Promote Artifact on Maven Central

Login to [Sonatype OSS Staging][2] and find the newly created staging repository. Select the repository and click "Close". Enter a description and click "Confirm".

Example:

> Promote artifact lost-1.1.0

### 2. Release Artifact to Maven Central

Login to [Sonatype OSS Staging][3] and find the promoted staging repository from step 2. Select the repository and click "Release". Enter a description and click "Confirm".

Example:

> Release artifact lost-1.1.0

**Note: It may take up to two hours for new artifacts to appear in [Maven Central][3].**

For more information see the official [Sonatype OSS Maven Repository Usage Guide][4].

### 3. Update Documentation

Update the README download instructions to point to the newly released artifact. Add release notes and attach the artifact to the list of [releases][5].

[1]:https://oss.sonatype.org/#view-repositories;snapshots~browsestorage
[2]:https://oss.sonatype.org/#stagingRepositories
[3]:http://search.maven.org/
[4]:https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
[5]:https://github.com/mapzen/lost/releases
