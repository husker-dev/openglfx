<script>
	function hasNavigation() { return false; }
    function getTitle() { return "Games"; }
</script>

<style>
	.game {
		display: flex;
		flex-wrap: wrap;
		gap: 20pt;
	}

	.game img {
		width: 250pt;
		height: 250pt;
		object-fit: cover;
		border-radius: 10px;
		background: rgba(0, 0, 0, 0);
		animation-duration: 0.8s;
		animation-name: fade;
	}

	.game h1 {
		margin-top: 0px;
	}

	.game-table {
		display: flex;
		gap: 20pt;
		color: var(--color-text-2);
		flex-wrap: wrap;
		width: 300pt;
	}

	.game-table div div {
		color: var(--color-text-3);
		
	}
</style>

# Info

Here are the games that I actively play

<div class="page-separator-close"></div>
<br/>

<div class="game">
	<img src="resources/profile/games/csgo.jpg" />
	<div>
		<h1 id="csgo">Counter-Strike: Global Offensive</h1>
		<div class="game-table">
			<div><div>In game</div>>1900 hours</div>
			<div><div>Since</div>2015</div>
			<div><div>Max rank</div>Master Guardian Elite</div>
			<div><div>Max wingman rank</div>Supreme Master First Class</div>
			<div><div>VAC banned</div>yes :(</div>
		</div>
	</div>
</div>
<hr/>

<div class="game">
	<img src="resources/profile/games/osu.png" />
	<div>
		<h1 id="osu">osu!lazer</h1>
		<div class="game-table">
			<div><div>In game</div>3d 18h 23m</div>
			<div><div>Since</div>2019</div>
			<div><div>Level</div>64</div>
			<div><div>PP</div>0*</div>
		</div>
		<br/>
		<br/>
		<div style="color: var(--color-text-3)">* Lazer doesn't count pp</div>
	</div>
</div>
<hr/>

<div class="game">
	<img src="resources/profile/games/minecraft.jpg" />
	<div>
		<h1 id="minecraft">Minecraft</h1>
		<div class="game-table">
			<div><div>Since</div>2010</div>
		</div>
	</div>
</div>
<hr/>

<div class="game">
	<img src="resources/profile/games/halflife.jpg" />
	<div>
		<h1 id="halflife">Half-Life</h1>
		<div class="game-table">
			<div><div>Since</div>2013</div>
			<div><div>Speedrun</div>Under 1 hour</div>
		</div>
	</div>
</div>
<hr/>