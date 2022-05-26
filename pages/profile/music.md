<script>
	function hasNavigation() { return false; }
    function getTitle() { return "Music"; }

	function onPageLoad(){
		addArtist("AsperX", "asperx", 
			"bad_trip", "Bad Trip",
			"kosmos", "Космос",
			"sumashedshim_vhod_besplatno", "Сумасшедшим вход бесплатно"
		);
		addArtist("Лжедмитрий IV", "ljedmitriy", 
			"ljeblagodat", "Лжеблагодать",
			"bitva", "Битва",
			"helltrigger", "Хэллтриггер"
		);
		addArtist("Lodoss", "lodoss", 
			"vosmorka", "Восьмёрка",
			"megalodon", "Мегалодон",
			"jguchie", "Жгучие"
		);
		addArtist("Обстоятельства", "obstoyatelstva", 
			"v_moih_glazah", "В моих глазах",
			"stonut_minuti", "Стонут минуты",
			"zavodnoy_apelsin", "Заводной апельсин"
		);
		addArtist("ZOLOTO", "zoloto", 
			"poka", "Пока",
			"ulitsi_jdali", "Улицы ждали",
			"pmml", "PMML"
		);
		addArtist("Jubilee", "jubilee", 
			"kladbiche_imeni_menya", "Кладбище имени меня",
			"poisk", "Поиск",
			"bolno", "Больно"
		);
	}

	function addArtist(name, id, mus1, mus1_title, mus2, mus2_title, mus3, mus3_title){
		findById("artists").innerHTML += `
			<div class="artist table">
				<img src="resources/profile/music/${id}/image.jpg" id="image"/>
				<div id="samples">
					<h1 id="${id}">${name}</h1>

					<music-player src="resources/profile/music/${id}/${mus1}.mp3" name="${mus1_title}" singer="${name}" class="music"></music-player>
					<music-player src="resources/profile/music/${id}/${mus2}.mp3" name="${mus2_title}" singer="${name}" class="music"></music-player>
					<music-player src="resources/profile/music/${id}/${mus3}.mp3" name="${mus3_title}" singer="${name}" class="music"></music-player>
				</div>
			</div>
			<hr/>
		`
	}
</script>

<style>

.artist {
	gap: 5pt 30pt;
	justify-content: center;
}

.artist #image {
	width: 260pt;
	height: 260pt;
	display: inline-block;
	vertical-align: top;
	background: rgba(0, 0, 0, 0) !important;
	border-radius: 50%;
	object-fit: cover;
	animation-duration: 0.8s;
	animation-name: fade;
}

.artist h1 {
	font-size: 34pt !important;
	margin-bottom: 0pt !important;
	margin-top: 0pt !important;
}

.artist #samples {
	width: 270pt;
}

.music {
	margin-bottom: 8pt;
	width: 100%;
}

</style>

# Information

My favorite genre of music is **pop**, **rock** and **rap**. 

I listen to a lot of singers, but I would like to show the most important ones here. They are associated with certain periods of my life. 

So, I wish you enjoy it. 

<div class="page-separator-close"></div>
<br/>
<div id="artists"></div>
