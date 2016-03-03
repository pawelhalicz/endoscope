(function($) {

    function Endoscope(placeholder) {
        this.placeholder = placeholder;
        this.load();
    }

    Endoscope.prototype.load = function(){
        //Load table
        $.ajax("ui/data/top", {dataType: "json"})
            .done($.proxy(this.onTopLevelStatsLoad, this))
            .fail($.proxy(this.onTopLevelStatsError, this));
    };

    Endoscope.prototype.onTopLevelStatsError = function(){
        alert( "failed to load data");
    };

    Endoscope.prototype.onTopLevelStatsLoad = function(topLevelStats) {
        var esTable = $($("#es-table-template").html());
        this.placeholder.append(esTable);

        var table = esTable.find("tbody");
        forEachStat(topLevelStats, function(id, stat){
            var row = $(buildRowHtml(id, stat, 0));
            table.append(row);
            if( stat.children ){
                row.click(onRowClick);
            }
        });
    };

    var onRowClick = function() {
        var row = $(this);
        if( row.hasClass('expanded') ){
            removeChildStats(row);
        } else {
            if( !row.hasClass('loading') ){
                loadChildStats(row);
            }
        }
    };

    var removeChildStats = function(row) {
        row.nextUntil("tr.parent").remove()
        row.removeClass('expanded');
    };

    var loadChildStats = function(row) {
        row.addClass('loading');
        var statId = row.data('id');
        console.log('loading sub stats: ' +  statId);
        $.ajax("ui/data/sub/" + statId, {dataType: "json"})
            .done(function(stats){
                row.removeClass('loading');
                row.addClass('expanded');
                onReceiveChildStats(stats, row, 1);
            })
            .fail(function(){
                row.removeClass('loading');
                showLoadError();
            });
    };

    var onReceiveChildStats = function(parentStat, parentRow, level) {
        forEachStat(parentStat.children, function(id, childStat){
            var row = $(buildRowHtml(id, childStat, level));
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

    var buildRowHtml = function(id, obj, level){
        var clazz = '';
        var data = '';
        var count = obj.hits;
        if( obj.children ){
            clazz += " has-children";
        }
        if(level == 0){
            data = id;
            clazz += ' parent'
        } else {
            count = obj.ah10/10;
            clazz += ' child'
        }

        var html = '<tr class="' + clazz + '" data-id="' + data + '">' +
            '<td class="id"><span class="btn"></span> ' + indent(level) + id + '</td>' +
            '<td class="count">' + count + '</td>' +
            '<td class=max">' + obj.max + '</td>' +
            '<td class="min">' + obj.min + '</td>' +
            '<td class=avg">' + obj.avg + '</td>' +
            '</tr>\n';
        return html;
    };

    var indent = function(count){
        var result = '';
        for(var i=0; i<count; i++){
            result += '<span class="indent"></span>'
        }
        return result;
    };

    $.endoscope = function(placeholder){
        return new Endoscope($(placeholder));
    }
})(jQuery);

