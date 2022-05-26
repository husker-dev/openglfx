<script>
    function hasNavigation() { return false; }
    function getTitle() { return "Projects"; }

    function onPageLoad(){
        // Applications
        addProject('_applications', 'launcher', 
            `Minecraft Launcher`, `Beautiful Minecraft launcher wtitten in Kotlin`);
        addProject('_applications', 'archiesparadise', 
            `Archie's Paradise`, `Multifunctional minecraft launcher for ArchieTheRaccoon with an account system`);
        addProject('_applications', 'fandomfetcher', 
            `Fandom Fetcher`, `A tool for finding members of a fandom, subculture, community in your city using vk.com`);

        // Frameworks
        addProject('_frameworks', 'minui', 
            `MinUI`, `Lightweight crossplatform UI framework for Kotlin/Java`);

        // Libraries
        addProject('_libraries', 'openglfx', 
            `OpenGLFX`, `OpenGL implementation for JavaFX, that uses LWJGL or JOGL`);
        addProject('_libraries', 'nioex', 
            `NioEx`, `Advanced usage of Kotlin I/O`);
        addProject('_libraries', 'jmapviewer', 
            `JMapViewer`, `Simple map viewer for Swing`);
        addProject('_libraries', 'simplehttp', 
            `SimpleHttp`, `Very simple HTTP request library`);

        // Utilities
        addProject('_utilities', 'nativejava', 
            `Java Native Plugin`, `Gradle plugin for native Java compilation using GraalVM`);
        addProject('_utilities', 'weblafplugin', 
            `WebLaF Plugin`, `WebLaF support to Intellij IDEA, such as file associations, icons and editors`);
        addProject('_utilities', 'weblafdark', 
            `WebLaF Dark Skin`, `Dark skin for WebLaF`);

        // Html pages
        addProject('_pages', 'self', 
            `This site`, `Personal site based on static html`);
        addProject('_pages', 'furrymap', 
            `Furmap Wayback`, `Mixed data from furmap.net and furrymap.net`);

    }

    function addProject(target, id, name, description){
        findById(target).innerHTML += 
        `<div class="project" onmousedown="selectPage('projects/${id}', \`${name}\`)">
            <img src="resources/projects/${id}/preview.jpg" 
                    onerror="this.onerror=null;this.src='resources/projects/preview_empty.png';">
            <div>${name}</div>
            <div>${description}</div>
        </div>`
    }

</script>
<style>
    h1 {
        margin-top: 50pt !important;
    }

    .project {
        width: 196pt !important;
        height: 300px !important;
        border-radius: 8pt;
        padding-bottom: 5pt;
        cursor: pointer;
        box-shadow: 0px 4px 1px 1px rgba(0, 0, 0, 0.1);
        background: var(--color-4);

        animation-duration: 0.8s;
        animation-name: fade;
        transition: all 0.2s ease;
    }

    .project:hover {
        background: var(--color-5);
        transform: scale(1.003);
    }

    .project img {
        background: rgba(0, 0, 0, 0) !important;
        width: 100%;
        height: 120pt;
        object-fit: cover;
        border-radius: 10px 10px 0px 0px;
        animation-duration: 0.8s;
        animation-name: fade;
    }

    .project div:nth-of-type(1) {
        color: var(--color-text-1);
        font-size: 17pt;
        font-weight: 500;
        border-radius: 8pt; 
        margin: 8pt;
    }

    .project div:nth-of-type(2) {
        color: var(--color-text-3);
        font-size: 12pt;
        border-radius: 8pt; 
        margin: 8pt;
    }

    .empty img {
        visibility: hidden;
    }
</style>

<h1 id="applications" class="floating">Applications</h1>
<div id="_applications" class="table3"></div>

<h1 id="frameworks" class="floating">Frameworks</h1>
<div id="_frameworks" class="table3"></div>

<h1 id="libraries" class="floating">Libraries</h1>
<div id="_libraries" class="table3"></div>

<h1 id="utilities" class="floating">Utilities</h1>
<div id="_utilities" class="table3"></div>

<h1 id="pages" class="floating">Pages</h1>
<div id="_pages" class="table3"></div>