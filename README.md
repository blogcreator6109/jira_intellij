# Jira Commit Assistant IntelliJ Plugin

이 저장소는 커밋 메시지 앞에 자동으로 Jira 티켓 키를 붙여주는 IntelliJ IDEA 플러그인을 제공합니다. 커밋 시점에 최근 Jira 이슈 목록이 표시되고, 원하는 이슈를 선택하면 `[KEY] message` 형식으로 메시지가 채워집니다.

## 주요 기능
- **자동 프리픽스**: 커밋 버튼을 눌렀을 때 Jira 이슈 선택 창이 열리고, 선택한 티켓 키가 메시지 앞에 자동으로 붙습니다.
- **JQL 기반 목록**: 설정한 JQL을 이용해 최대 10개의 최근 이슈를 불러옵니다.
- **검색 지원**: 선택 창의 검색 필드를 통해 다른 티켓을 즉시 찾을 수 있습니다.
- **API 변경 대응**: Jira REST API v3 실패 시 v2로 자동 폴백하여 최신 변경 사항에 유연하게 대응합니다.

## 설정 방법
1. `File | Settings | Tools | Jira Commit Assistant` 메뉴를 열어 다음 정보를 입력합니다.
   - Jira Base URL (예: `https://your-domain.atlassian.net`)
   - User Email (Jira Cloud API 토큰을 사용하는 경우 이메일)
   - API Token
   - 기본 JQL (선택 사항, 비워두면 최신 업데이트 순으로 정렬)
2. 설정을 저장하면 다음 커밋부터 선택 창이 활성화됩니다.

## 빌드 및 설치
모든 의존성은 Gradle이 자동으로 내려받습니다. 저장소에는 Gradle Wrapper 스크립트(`./gradlew`)는 포함되어 있지만, 호스팅 제약으로 인해 `gradle-wrapper.jar` 바이너리는 버전에 맞춰 자동으로 내려받도록 구성했습니다. 아래 스크립트 한 줄이면 필요한 파일을 내려받고 플러그인 ZIP을 `build/distributions/` 아래에 만들어 줍니다.

```bash
./build_plugin.sh
```

스크립트는 다음을 수행합니다.

1. `gradle/wrapper/gradle-wrapper.properties`에 정의된 버전(현재 8.14.3)을 확인합니다.
2. 누락된 경우 해당 버전의 `gradle-wrapper.jar`를 인터넷에서 다운로드하거나, 필요하다면 배포 ZIP에서 추출합니다.
3. Gradle Wrapper(`./gradlew`)로 `clean buildPlugin`을 실행해 플러그인 패키지를 생성합니다.

빌드가 완료되면 `build/distributions/` 디렉터리에 `.zip` 파일이 생성됩니다. IntelliJ IDEA에서 `Settings | Plugins | ⚙ | Install Plugin from Disk...` 메뉴로 해당 ZIP을 선택해 설치할 수 있습니다.

> **참고**: `./gradlew buildPlugin`을 직접 실행하고 싶다면, 먼저 `./build_plugin.sh`를 한 번 실행해 `gradle-wrapper.jar`를 내려받은 뒤 사용하면 됩니다. 또는 `gradle wrapper --gradle-version 8.14.3` 명령으로 동일한 버전의 Wrapper를 생성해도 됩니다.
=======
모든 의존성은 Gradle이 자동으로 내려받습니다. 다음 명령 한 줄로 플러그인 패키지를 생성할 수 있습니다.

```bash
./gradlew buildPlugin
```

빌드가 완료되면 `build/distributions/` 디렉터리에 `.zip` 파일이 생성됩니다. IntelliJ IDEA에서 `Settings | Plugins | ⚙ | Install Plugin from Disk...` 메뉴로 해당 ZIP을 선택해 설치할 수 있습니다.

만약 Gradle Wrapper를 사용할 수 없는 환경이라면 `gradle buildPlugin` 명령을 실행해도 동일하게 동작합니다.

## 배포 스크립트 (선택 사항)
간단한 쉘 스크립트 `build_plugin.sh`를 제공하니 필요에 따라 사용하세요.

```bash
./build_plugin.sh
```

해당 스크립트는 기존 산출물을 정리한 후 플러그인 ZIP 파일을 생성합니다.