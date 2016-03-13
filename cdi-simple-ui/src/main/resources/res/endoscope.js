(function($) {
    var defaults = {
        valueBadLevel:  300, //3000
        valueWarnLevel: 100, //1000
        statUrl: "ui/data/sub/{id}?from=0&to=0",
        topUrl: "ui/data/top?from=0&to=0"
    };

    function Endoscope(placeholder) {
        this.placeholder = placeholder;
        $.ajax(defaults.topUrl, {dataType: "json"})
            .done($.proxy(onTopLevelStatsLoad, this))
            .fail(function(){showError("Failed to load stats data")});
    }

    var showError = function(text){
        var err = $("#es-error");
        err.find(".text").text(text);
        err.show();
    };

    var onTopLevelStatsLoad = function(topLevelStats) {
        var esTable = $($("#es-table-template").html());
        this.placeholder.append(esTable);

        var table = esTable.find("tbody");
        forEachStat(topLevelStats, function(id, stat){
            var row = buildRow(id, stat, 0);
            table.append(row);
            if( stat.children ){
                row.click(onRowClick);
            }
        });
    };

    var onRowClick = function() {
        var row = $(this);
        if( row.hasClass('es-expanded') ){
            removeChildStats(row);
        } else {
            if( !row.hasClass('es-loading') ){
                loadChildStats(row);
            }
        }
    };

    var removeChildStats = function(row) {
        row.nextUntil("tr.es-parent").remove();
        row.removeClass('es-expanded');
    };

    var loadChildStats = function(row) {
        row.addClass('es-loading');
        var statId = row.data('id');
        $.ajax(defaults.statUrl.replace("{id}", statId), {dataType: "json"})
            .done(function(stats){
                row.removeClass('es-loading');
                row.addClass('es-expanded');
                onReceiveChildStats(stats, row, 1);
            })
            .fail(function(){
                row.removeClass('es-loading');
                showError("Failed to load child stats");
            });
    };

    var onReceiveChildStats = function(parentStat, parentRow, level) {
        forEachStat(parentStat.children, function(id, childStat){
            var row = $(buildRow(id, childStat, level));
            parentRow.after(row);
            onReceiveChildStats(childStat, row, level+1)
        });
    };

    var forEachStat= function(stat, fn){
        if(stat == null){
            return;
        }
        for (var id in stat) {
            if (stat.hasOwnProperty(id)) {
                fn(id, stat[id]);
            }
        }
    };

    var buildRow = function(id, obj, level){
        var row = $($("#es-row-template").html());

        if( obj.children ){
            row.addClass("es-has-children");
        }
        if(level == 0){
            row.attr("data-id", id);
            row.addClass("es-parent");
            row.find(".es-count").append(obj.hits);
        } else {
            row.addClass("es-child");
            row.find(".es-count").append(obj.ah10/10);
        }

        row.find(".es-id").append(indent(level)).append(id);
        addNumberValue( row.find(".es-max"), obj.max);
        addNumberValue( row.find(".es-min"), obj.min);
        addNumberValue( row.find(".es-avg"), obj.avg);

        return row;
    };

    var addNumberValue = function(el, val){
        var tpl = valueTemplate(val);
        if( tpl ){
            val = $($(tpl).html()).text(val);
        }
        el.append(val);
    };

    var valueTemplate = function(time){
        if( time > defaults.valueBadLevel ){
            return "#es-bad-template"
        }
        if( time > defaults.valueWarnLevel ){
            return "#es-warn-template"
        }
        return null;
    };

    var indent = function(count){
        var indentHtml = $("#es-indent-template").html();
        var result = '';
        for(var i=0; i<count; i++){
            result += indentHtml;
        }
        return $(result);
    };

    $.endoscope = function(placeholder){
        return new Endoscope($(placeholder));
    }
})(jQuery);

