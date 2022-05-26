<style>
	.config > .title{
		font-size: 20pt;
		border-bottom: 2px solid var(--color-text-3);
		margin-bottom: 7pt;
		margin-right: 10pt;
	}

	.config > div:not(.title){
		margin-bottom: 4pt;
	}

	.config > label {
		font-size: 12pt;
	}

	.invisible {
		display: none;
	}
</style>
<script>
	function hasNavigation() { return false; }

	var version = "[load error]"

	function onPageLoad(){
		loadURLContent("https://api.github.com/repos/husker-dev/openglfx/releases/latest", text => {
			version = JSON.parse(text)["tag_name"];
			updateCode();
		})
	}

	var maven_repositories = 
`<repositories>
	<repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>`

	var maven_dependency = 
`<!-- OpenGLFX -->
<dependency>
    <groupId>com.github.husker-dev.openglfx</groupId>
    <artifactId>core</artifactId>
    <version>$version</version>
</dependency>
<dependency>
    <groupId>com.github.husker-dev.openglfx</groupId>
    <artifactId>$module</artifactId>
    <version>$version</version>
</dependency>

<!-- Kotlin lib -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib-jdk8</artifactId>
    <version>RELEASE</version>
</dependency>`

	var gradle_repositories = 
`repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}`
	var gradle_dependency = 
`dependencies {
    // OpenGLFX
    implementation 'com.github.husker-dev.openglfx:core:$version'
    implementation 'com.github.husker-dev.openglfx:$module:$version'

    // Kotlin lib
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
}`

	var sbt_repositories = 
`resolvers += "jitpack" at "https://jitpack.io"`
	
	var sbt_dependency = 
`// OpenGLFX
libraryDependencies += "com.github.husker-dev.openglfx" % "core" % "$version"
libraryDependencies += "com.github.husker-dev.openglfx" % "$module" % "$version"

// Kotlin lib
libraryDependencies += "org.jetbrains.kotlin" % "kotlin-stdlib-jdk8" % "RELEASE"`

	var java_example = 
`OpenGLCanvas canvas = OpenGLCanvas.create($module);
canvas.setAnimator(new GLCanvasAnimator(60.0));

canvas.addOnInitializeEvent((event) -> {
    $getter
});
canvas.addOnReshapeEvent((event) -> {
    $getter
});
canvas.addOnRenderEvent((event) -> {
    $getter
});
canvas.addOnDisposeEvent((event) -> {
	$getter
});`

	var kotlin_example =
`val canvas = OpenGLCanvas.create($module)
canvas.animator = GLCanvasAnimator(60.0);

canvas.onInitialize { 
    $getter
}
canvas.onReshape { 
    $getter
}
canvas.onRender { 
    $getter
}
canvas.onDispose {
    $getter
}`

	function updateCode(radio){
		const lwjgl = findById("radio_lwjgl");
		const jogl = findById("radio_jogl");

		const gradle = findById("radio_gradle");
		const block_gradle = findById("gradle-block");
		const code_gradle = block_gradle.querySelector('#groovy-code');
		const code_gradle2 = block_gradle.querySelector('#groovy-code2');

		const maven = findById("radio_maven");
		const block_maven = findById("maven-block");
		const code_maven = block_maven.querySelector('#maven-code');
		const code_maven2 = block_maven.querySelector('#maven-code2');

		const sbt = findById("radio_sbt");
		const block_sbt = findById("sbt-block");
		const code_sbt = block_sbt.querySelector('#sbt-code');
		const code_sbt2 = block_sbt.querySelector('#sbt-code2');

		const kotlin = findById("radio_kotlin");
		const block_kotlin = findById("kotlin-block");
		const code_kotlin = block_kotlin.querySelector('#kotlin-code');

		const java = findById("radio_java");
		const block_java = findById("java-block");
		const code_java = block_java.querySelector('#java-code');

		if((lwjgl.checked || jogl.checked) && (gradle.checked || maven.checked || sbt.checked) && (kotlin.checked || java.checked)){
			const isLWJGL = lwjgl.checked;
			const isGradle = gradle.checked;
			const isMaven = maven.checked;
			const isKotlin = kotlin.checked;

			if(isGradle){
				block_gradle.classList.remove("invisible");
				block_maven.classList.add("invisible");
				block_sbt.classList.add("invisible");

				putCode(code_gradle, "groovy", gradle_repositories);
				putCode(code_gradle2, "groovy", gradle_dependency
					.replaceAll("$module", isLWJGL? "lwjgl" : "jogl")
					.replaceAll("$version", version)
				);
			}else if(isMaven){
				block_gradle.classList.add("invisible");
				block_maven.classList.remove("invisible");
				block_sbt.classList.add("invisible");

				putCode(code_maven, "xml", maven_repositories);
				putCode(code_maven2, "xml", maven_dependency
					.replaceAll("$module", isLWJGL? "lwjgl" : "jogl")
					.replaceAll("$version", version)
				);
			}else {
				block_gradle.classList.add("invisible");
				block_maven.classList.add("invisible");
				block_sbt.classList.remove("invisible");

				putCode(code_sbt, "scala", sbt_repositories);
				putCode(code_sbt2, "scala", sbt_dependency
					.replaceAll("$module", isLWJGL? "lwjgl" : "jogl")
					.replaceAll("$version", version)
				);
			}

			if(isKotlin){
				block_kotlin.classList.remove("invisible");
				block_java.classList.add("invisible");

				putCode(code_kotlin, "kotlin", kotlin_example
					.replace("$module", isLWJGL? "LWJGL_MODULE" : "JOGL_MODULE")
					.replaceAll("$getter", isLWJGL? "" : "val gl = (canvas as JOGLEvent).gl\n")
					);
			}else{
				block_java.classList.remove("invisible");
				block_kotlin.classList.add("invisible");

				putCode(code_java, "java", java_example
					.replaceAll("$module", isLWJGL? "LWJGLExecutor.LWJGL_MODULE" : "JOGLExecutor.JOGL_MODULE")
					.replaceAll("$getter", isLWJGL? "" : "GL2 gl = ((JOGLEvent) event).getGl();\n")
					);
			}
		}
	}
</script>

# OpenGLFX Generator

Tool to create proper OpenGLFX configuration

<div class="page-separator"></div>

<div class="table3">
	<div class="config">
		<div class="title">OpenGL Library</div>
		<div>
			<input name="lib" type="radio" id="radio_lwjgl" onclick="updateCode()">
			<label for="radio_lwjgl">LWJGL</label>
		</div>
		<div>
			<input name="lib" type="radio" id="radio_jogl" onclick="updateCode()">
			<label for="radio_jogl">JOGL</label>
		</div>
	</div>
	<div class="config">
		<div class="title">Build engine</div>
		<div>
			<input name="build-engine" type="radio" id="radio_gradle" onclick="updateCode()">
			<label for="radio_gradle">Gradle</label>
		</div>
		<div>
			<input name="build-engine" type="radio" id="radio_maven" onclick="updateCode()">
			<label for="radio_maven">Maven</label>
		</div>
		<div>
			<input name="build-engine" type="radio" id="radio_sbt" onclick="updateCode()">
			<label for="radio_sbt">Sbt</label>
		</div>
	</div>
	<div class="config">
		<div class="title">Language</div>
		<div>
			<input name="language" type="radio" id="radio_kotlin" onclick="updateCode()">
			<label for="radio_kotlin">Kotlin</label>
		</div>
		<div>
			<input name="language" type="radio" id="radio_java" onclick="updateCode()">
			<label for="radio_java">Java</label>
		</div>
	</div>
</div>

<div id="gradle-block" class="invisible">
	<h2>Gradle</h2>
	<div id="groovy-code"></div>
	<div id="groovy-code2"></div>
</div>

<div id="maven-block" class="invisible">
	<h2>Maven</h2>
	<div id="maven-code"></div>
	<div id="maven-code2"></div>
</div>

<div id="sbt-block" class="invisible">
	<h2>Sbt</h2>
	<div id="sbt-code"></div>
	<div id="sbt-code2"></div>
</div>

<div id="kotlin-block" class="invisible">
	<h2>Kotlin</h2>
	<div id="kotlin-code"></div>
</div>

<div id="java-block" class="invisible">
	<h2>Java</h2>
	<div id="java-code"></div>
</div>