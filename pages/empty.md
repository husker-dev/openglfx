<script>
	function hasNavigation() { return false; }
	function getTitle() { return "404"; }
</script>

<style>
	#empty-image {
		width: 160pt;
		border-radius: 12px;
		background: rgba(0, 0, 0, 0);
		animation-duration: 0.8s;
  		animation-name: fade;
	}
	#back {
		cursor: pointer;
		width: 160pt;
		height: 30pt;
		background: var(--color-5);
		color: var(--color-text-2);
		display: flex;
		justify-content: center;
		align-items: center;
		border-radius: 7px;
		margin-top: 60pt;
		box-shadow: 0px 0px 5px 5px rgba(0, 0, 0, 0.05);
	}

	#back:hover {
		background: var(--color-6);
		box-shadow: 0px 0px 5px 5px rgba(0, 0, 0, 0.1);
	}

	.content {
		justify-content: center;
	}
</style>

<div class="table content">
	<img id="empty-image" src="resources/empty.jpg"/>
	<div>
		<h1 style="margin-top: 0px">Empty page</h1>
		Here is nothing.
		<div id="back" onclick="window.history.go(-1);">Go back</div>
	</div>
</div>




