(function($) {
    var options = {
        valueBadLevel:  300, //3000
        valueWarnLevel: 100, //1000
        statUrl: "ui/data/sub/{id}",
        topUrl: "ui/data/top",
        from: null,
        to: null,
        past: 3600000, //1 hour,
        sortField: 'id',
        sortDirection: 1
    };

    var placeholder;

    function Endoscope(_placeholder) {
        placeholder = _placeholder;

        loadTopLevel();

        $("#es-past").change(function(){
            options.past = $(this).val();
            options.from = null;
            options.to = null;

            loadTopLevel();
        });

        $("#es-search").typeWatch({ //https://github.com/dennyferra/TypeWatch
            callback: onSearch,
            wait: 500,
            highlight: true,
            captureLength: 2
        });
    }

    var onSearch = function(value){
        value = value.toLowerCase();

        closeAllRows();

        placeholder.find("tbody tr.es-parent").each(function(index, row){
            row = $(row);
            if( row.data().id.toLowerCase().indexOf(value) >= 0){
                row.show();
            } else {
                row.hide();
            }
        });
    };

    var closeAllRows = function(){
        placeholder.find("tbody tr.es-expanded").each(function(index, row){
            removeChildStats($(row));
        });
    };

    var loadTopLevel = function(){
        $.ajax(options.topUrl, {
            dataType: "json",
            data: {
                from: options.from,
                to: options.to,
                past: options.past
            }
        })
        .done($.proxy(onTopLevelStatsLoad, this))
        .fail(function(){showError("Failed to load stats data")});
    };

    var showError = function(text){
        var err = $("#es-error");
        err.find(".text").text(text);
        err.show();
    };

    var onTopLevelStatsLoad = function(topLevelStats) {
        placeholder.empty();
        var esTable = $($("#es-table-template").html());
        placeholder.append(esTable);

        var table = esTable.find("tbody");
        var orderedStats = [];
        forEachStat(topLevelStats, function(id, stat){
            stat.id = id;
            orderedStats.push( stat );
        });
        orderedStats.sort(onSortTopLevel);
        orderedStats.forEach(function(stat){
            var row = buildRow(stat.id, stat, 0);
            table.append(row);
            if( stat.children ){
                row.click(onRowClick);
            }
        });

        var headers = esTable.find("thead th");
        headers.click(onSortColumnClick);
        headers.each(function(index, th){
            th = $(th);
            var span = th.find("span");
            if(options.sortField == th.data().sort){
                if( options.sortDirection> 0 ){
                    span.addClass("glyphicon glyphicon-sort-by-attributes");
                } else {
                    span.addClass("glyphicon glyphicon-sort-by-attributes-alt");
                }
            } else {
                span.removeClass("glyphicon glyphicon-sort-by-attributes glyphicon-sort-by-attributes-alt");
            }
        });
    };

    var onSortColumnClick = function(){
        var th = $(this);
        var colId = th.data().sort;
        if( colId == options.sortField ){
            options.sortDirection *= -1;
        } else {
            options.sortField = colId;
            options.sortDirection = colId == "id" ? 1 : -1;
        }
        loadTopLevel();
    };

    var onSortTopLevel = function(a, b){
        switch(options.sortField){
            case "hits": return (a.hits - b.hits) * options.sortDirection;
            case "min":  return (a.min - b.min)   * options.sortDirection;
            case "max":  return (a.max - b.max)   * options.sortDirection;
            case "avg":  return (a.avg - b.avg)   * options.sortDirection;
            default:
                if( a.id == b.id ){
                    return 0;
                }
                return (a.id < b.id ? -1 : 1) * options.sortDirection;
        }
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
        $.ajax(options.statUrl.replace("{id}", statId), {
            dataType: "json",
            data: {
                from: options.from,
                to: options.to,
                past: options.past
            }
        })
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
        if( time > options.valueBadLevel ){
            return "#es-bad-template"
        }
        if( time > options.valueWarnLevel ){
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

