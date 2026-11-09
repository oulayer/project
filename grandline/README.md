# Grand Line - Frutas e Haki (Forge 1.20.1)

## Compilar
1. Instale o JDK 17.
2. Copie `gradlew`, `gradlew.bat` e a pasta `gradle/` do MDK oficial do Forge 1.20.1 (47.2.x) para esta pasta
   (ou rode `gradle wrapper` se tiver o Gradle instalado).
3. `./gradlew build`  ->  jar em `build/libs/`. Para testar: `./gradlew runClient`.

## Controles (alteráveis em Opções > Controles > Grand Line)
Z / X / C = habilidades da fruta | G = Haki do Armamento | V = Haki da Observação | B = Haki do Rei

## Comandos (op)
/grandline set <rubber|flame|ice|quake>   /grandline reset

## Compilar sem instalar nada (GitHub)
1. Crie um repositório no GitHub e envie todo o conteúdo desta pasta (incluindo `.github`).
2. Abra a aba **Actions**, aguarde o build "Build mod" terminar (uns 5 min).
3. Entre na execução e baixe o artefato **grandline-jar**: dentro está o .jar do mod.
