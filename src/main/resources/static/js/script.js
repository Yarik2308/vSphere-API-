/**
 *
 * @param ctx ссылка на контекст чертежа
 * @param centerX  координата X центра окружности
 * @param centerY координата Y центра окружности
 * @param radius координата X конечной точки линии
 * @param startAngle угол начала в радианах, где начинается часть круга
 * @param endAngle конечный угол в радианах, где заканчивается часть круга
 * @param color цвет, используемый для заполнения среза
 */
function drawPieSlice(ctx,centerX, centerY, radius, startAngle, endAngle, color ){
    ctx.fillStyle = color;
    ctx.beginPath();
    ctx.moveTo(centerX,centerY);
    ctx.arc(centerX, centerY, radius, startAngle, endAngle);
    ctx.closePath();
    ctx.fill();
}

/**
 *
 * @param options объект, содержащий следующее:
     canvas: ссылка на холст, где мы хотим нарисовать круговую диаграмму
     data: ссылка на объект, содержащий модель данных
     colors: массив, содержащий цвета, которые мы хотим использовать для каждого фрагмента
 * @constructor
 */
var PieChart = function(options){
    this.options = options;
    this.canvas = options.canvas;
    this.ctx = this.canvas.getContext("2d");
    this.colors = options.colors;

    this.draw = function(){
        var total_value = 0;
        var color_index = 0;
        for (var category in this.options.data){
            var val = this.options.data[category];
            total_value += val;
        }

        var start_angle = 0;
        for (category in this.options.data){
            val = this.options.data[category];
            var slice_angle = 2 * Math.PI * val / total_value;

            drawPieSlice(
                this.ctx,
                this.canvas.width/2,
                this.canvas.height/2,
                Math.min(this.canvas.width/2,this.canvas.height/2),
                start_angle,
                start_angle+slice_angle,
                this.colors[color_index%this.colors.length]
            );

            start_angle += slice_angle;
            color_index++;
        }

        // text inside fragments
        start_angle = 0;
        for (category in this.options.data){
            val = this.options.data[category];
            var slice_angle = 2 * Math.PI * val / total_value;

            var pieRadius = Math.min(this.canvas.width / 2, this.canvas.height / 2);
            var labelX = this.canvas.width / 2 + (pieRadius / 2) * Math.cos(start_angle + slice_angle / 2);
            var labelY = this.canvas.height / 2 + (pieRadius / 2) * Math.sin(start_angle + slice_angle / 2);
            var labelText = Math.round(100 * val / total_value);
            this.ctx.fillStyle = "white";
            this.ctx.font = "bold 20px Arial";
            this.ctx.fillText(labelText + "%", labelX, labelY);

            start_angle += slice_angle;
    }

        // Legend
        if (this.options.legend){
            color_index = 0;
            var legendHTML = "";
            for (category in this.options.data){
                legendHTML += "<div><span style='display:inline-block;width:20px;background-color:"+this.colors[color_index++]+";'>&nbsp;</span> "+category+"</div>";
            }
            this.options.legend.innerHTML = legendHTML;
        }
    }
};

