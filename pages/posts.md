<script>

	function hasNavigation() { return false; }
	function getTitle() { return "Posts"; }

	function onPageLoad(){
		if(localStorage.lastCachedPage !== undefined)
			doAllStuff(localStorage.lastCachedPage);
		
		binaryPageSearch(0, 1024, last => {
			if(localStorage.lastCachedPage != last)
				doAllStuff(last);
			localStorage.lastCachedPage = last;
		})
	}

	function doAllStuff(last){
		var current = last;
		var lastPost = addPage(current, getPageContent(current));

		findById("total").innerHTML = `Smart thoughts: ${last}`;

		function checkLastPost(){
			while(current != 1 && isInViewport(lastPost)){
				current--;
				lastPost = addPage(current, getPageContent(current));
			}
		}

		window.addEventListener('scroll', function(e) {
			checkLastPost();
		});
		checkLastPost();
	}

	function getPageContent(i){
		var rawFile = new XMLHttpRequest();
	    rawFile.open("GET", `${window.location.origin}/pages/posts/${i}.md`, false);
	    rawFile.send();
        return rawFile.responseText;
	}

	function binaryPageSearch(from, to, callback){
		if(from == to - 1){
			callback(from);
			return;
		}
		var middle = parseInt((from + to) / 2);
		doesFileExistAsync(`/pages/posts/${middle}.md`, exist => {
			if(exist)
				binaryPageSearch(middle, to, callback);
			else
				binaryPageSearch(from, middle, callback);
		});
	}

	function addPage(i, text){
		var formatted = text.replace(/\s+/g, " ");
		var meta = formatted.split("<!--")[1].split("-->")[0];
		var title = meta.split("$title:")[1].split("$")[0].trim();
		var date = meta.split("$date:")[1].split("$")[0].trim();

		var content = text.split("-->")[1].split("\n").slice(0, 15).join("\n");
		if(content.length > 600)
			content = content.slice(0, 600);

		findById("pages-list").innerHTML += `
			<div class="page-separator"></div>
			<div class="blog" id="id:${i}">
				<div class="title noselect" onclick="selectPage('posts/${i}')">${title}</div>
				<div class="date">${date}</div>
				<zero-md no-shadow="true">
				  	<script type="text/markdown">
${content} ...
			  		<\/script>
				</zero-md>
				<div class="more" onclick="selectPage('posts/${i}')">Read more</div>
			</div>
		`;
		return findById(`id:${i}`);
	}

</script>

<style>
	#head {
		gap: 20pt;
		display: flex;
		justify-content: center;
	}

	#head h1 {
		margin-top: 0px;
	}

	#head > img {
		background: rgba(0, 0, 0, 0);
		width: 90pt;
		height: 90pt;
	}

	#total {
		color: var(--color-text-3);
	}

	.blog {
		margin-top: -10pt;
	}

	.blog > .title {
		font-size: 22pt;
		color: var(--color-text-1);
		font-weight: 400;
		border-bottom: 2px solid var(--color-text-3);
		cursor: pointer;
	}

	.blog > .title:hover {
		border-bottom: 2px solid var(--color-text-2);
	}

	.blog > .date {
		font-size: 10pt;
		margin-top: 5pt;
		color: var(--color-text-2);
	}

	.blog > .more {
		width: 100pt;
		border: 1px solid var(--color-text-3);
		
		margin-top: 5pt;
		margin-bottom: -5pt;
		padding: 8px;
		border-radius: 5px;
		display: flex;
		justify-content: center;
		transition: all 0.18s ease;
		cursor: pointer;
	}

	.blog > .more:hover {
		border: 1px solid var(--color-text-2);
	}

	.blog > zero-md > .markdown-body {
		padding: 10pt;
		
	}

	.blog > zero-md > .markdown-body * {
		color: var(--color-text-3) !important;
	}
</style>

<div id="head">
	<img src="resources/posts/thinking.png"/>
	<div>
		<h1>My Posts</h1>
		Here are my thoughts
		<br>
		<a id="total">Smart thoughts: --</a>
	</div>
</div>

<div id="pages-list"></div>