<style>
	img {
		border-radius: 5pt;
	}
	.main-pane {
		display: inline-flex;
		gap: 30px;
		flex-direction: column;
    	align-items: center;
    	width: 100%;
	}
	.main-pane > img {
		width: 100%;
		max-width: 300pt;
	}
	.main-pane > div {
		display: flex;
		gap: 10px;
		flex-direction: column;
		width: 100%;
		max-width: 300pt;
	}
	.field {
		display: flex;
		gap: 15px;
	}
	.field > input {
		width: 100%;
	}
	.button {
		margin-top: 10pt;
	}
</style>
<script>
	function hasNavigation() { return false; }

	function calculate(){
		var a = findById("a");
		var b = findById("b");
		var c = findById("c");

		if(a.value == ""){
			var b1 = myEval(b.value);
			var c1 = myEval(c.value);

			a.value = Math.sqrt(c1*c1 - b1*b1);
		}
		else if(b.value == ""){
			var a1 = myEval(a.value);
			var c1 = myEval(c.value);

			b.value = Math.sqrt(c1*c1 - a1*a1);
		}
		else if(c.value == ""){
			var a1 = myEval(a.value);
			var b1 = myEval(b.value);

			c.value = Math.sqrt(a1*a1 + b1*b1);
		}
	}

	function myEval(text){
		text = text.replace("sqrt(", "Math.sqrt(");
		text = text.replace("pow(", "Math.pow(");
		return parseFloat(eval(text));
	}
</script>

# Теорема Пифагора

Этот калькулятор находит неизвестную сторону по заданным данным


<div class="page-separator"></div>

<div class="main-pane">
	<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAOwAAACUCAMAAACnfDWKAAAASFBMVEX///8AAADb//8AOpD//7ZmAADDw8P/tmZmtv+2ZgCQOgD/25A6kNsAADqQ2////9vbkDoAAGa2//86AAAAZrY6OpAAZpCQZgBIgm3UAAACrklEQVR4nO3c21LCMBQFUKIgFFTq/f//1LRgh0opTXLuOfvBF5XsNW1OijOyWnk8Ho/H4/F4PBkJIXBXoEsIFXFDqIgbatJ2zmq4J2Ul3D9jFdxBWMPevfDZ54501rn/bLa1VzTL3AmYXe4kyyp3GmV0Ut0imeTeBhnkznHMaec1xrj3LKa49yWGuAscdibVIoUV7kKDDe5igQVtAkA/N6m+dm5ied3c5OqauenFFQ/mnNpquXmllXJzK6vU5jdWyC3pq45b1lYZt7CrrklV3FQTF6CnHi5ISy1aoJJ3uNvuu48PECuVBOyKzHGb8Pa+OnzZwc5w43V9hlqkKJB77dakasPLK9giJYEdLNPcxiZ2mmsWO7V1457dAS+SF4zz8YprcBqPX3X0wq2xc/b6deU9VKE1kvjEjNhHBLfpO+yGRngrCeA2u4snOOQu/NqY4/48HNGrcHP7N1xUWF7uehOf3uiu7HkRJm7/8EaL5ZtULQOWjXv4iIN4S7hnz+HhdvPpm/rKntfiPYdoV2fmUq/NyqVfmZHLsS4bl2VVrmOX6Y7i4bJNCw4u40lAr2U95am5zG+tabnsf0eg5LJjKScVP5aQKwFLxpWBJdq6UrAkXDlYAq4kLDpXFhZ5UgnD4nLFYTG5ArF4W1ckFosrFIvDFYvF4ArGwk8qyVhwrmwsMFc6FnTryscCcjVgwbg6sEBcLViQSaUGC8FVhC3nqsKWbl1l2DKuOmwJVyE2n6sSm8tVis0bzPEXnvrgdEJMBvfvx/VhM7iasclbVzc2kasdm8TVj03gBkNxbLVYj8czynoj4L+fqeJYq6kO+xOkfAIHciI27I777kMp7Od0G4v5vBHcnLD9f3Haz4D95G5CkOE2rufKthXt2filhru4P3piqrB6PB6Px+PxmMovxDYLekMMYskAAAAASUVORK5CYII=">
	<div>
		<div class="field">
			a: <input id="a">
		</div>
		<div class="field">
			b: <input id="b">
		</div>
		<div class="field">
			c: <input id="c">
		</div>
		<div class="button" onclick="calculate()">Посчитать</div>
	</div>
</div>