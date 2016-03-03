(function($) {

    function Endoscope(placeholder) {
        this.placeholder = placeholder;
        $.ajax("ui/data/top", {dataType: "json"})
            .done($.proxy(onTopLevelStatsLoad, this))
            .fail(onTopLevelStatsError);
    }

    var onTopLevelStatsError = function(){
        alert( "failed to load data");
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
        $.ajax("ui/data/sub/" + statId, {dataType: "json"})
            .done(function(stats){
                row.removeClass('es-loading');
                row.addClass('es-expanded');
                onReceiveChildStats(stats, row, 1);
            })
            .fail(function(){
                row.removeClass('es-loading');
                showLoadError();
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
        row.find(".es-max").append(obj.max);
        row.find(".es-min").append(obj.min);
        row.find(".es-avg").append(obj.avg);

        return row;
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

