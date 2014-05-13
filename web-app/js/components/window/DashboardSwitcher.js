Ext.define('Ozone.components.window.DashboardSwitcher', {
    extend: 'Ozone.components.window.ModalWindow',
    alias: 'widget.dashboardswitcher',
    
    closeAction: 'hide',
    modal: true,
    preventHeader: true,
    modalAutoClose: true,
    shadow: false,
    layout: 'auto',
    ui: 'system-window',
    store: null,
    closable: false,
    title: 'Dashboards',
    iconCls: 'dashboard-switcher-header-icon',
    cls: 'system-window',
    resizable: false,
    draggable: false,

    viewId: 'dashboard-switcher-dashboard-view',

    dashboardContainer: null,

    //dashboard unit sizes
    dashboardItemHeight: 0,
    dashboardItemWidth: 0,

    //size of switcher in dashboard units
    minDashboardsWidth: 3,
    maxDashboardsWidth: 5,
    maxDashboardsHeight: 3,

    storeLengthChanged: true,

    selectedItemCls : 'dashboard-selected',

    _deletedStackOrDashboards: null,

    DROP_LEFT_CLS: 'x-view-drop-indicator-left',
    DROP_RIGHT_CLS: 'x-view-drop-indicator-right',


    _previouslyFocusedStackOrDashboard : null,
    
    initComponent: function() {

        var me = this,
            stackOrDashboards = [],
            dashboards = {},
            dashboard, model;

        for (var i = 0, len = me.dashboardStore.getCount(); i < len; i++) {
			model = me.dashboardStore.getAt(i);

			dashboard = Ext.clone(model.data);
			dashboard.model = model;
			dashboards[dashboard.guid] = dashboard;
			stackOrDashboards.push(dashboard);
		}

        me.callParent(arguments);

        me.stackOrDashboards = stackOrDashboards;
        me.dashboards = dashboards;
        me._deletedStackOrDashboards = [];

        me.tpl = new Ext.XTemplate(
            '<tpl for=".">',
                '<div id="{[this.getName(values)+this.getId(values)]}" class="{[this.getClass(values)]}" tabindex="0" data-{[this.getName(values)]}-id="{[this.getId(values)]}" {[this.getToolTip(values)]}>',
                    '<div class="thumb-wrap">',
                        '<div class="thumb {layout}">',
                        '</div>',
                    '</div>',
                    '{[this.getActions(values)]}',
                    '<div class="{[this.getName(values)]}-name">',
                        '{[this.encodeAndEllipsize(values.name)]}',
                    '</div>',
                '</div>',
            '</tpl>'
        ,{
            compiled: true,
            getId: function (values) {
                return values.isStack ? values.id : values.guid;
            },
            getClass: function (values) {
                var name = this.getName(values);
                return values.guid === me.activeDashboard.id ? name + ' ' + me.selectedItemCls: name;
            },
            getName: function (values) {
                return 'dashboard';
            },
            getToolTip: function (values) {
                var str = 'data-qtip="<div class=\'dashboard-tooltip-content\'>' +
                        '<h3 class=\'name\'>' + Ext.htmlEncode(Ext.htmlEncode(values.name)) + '</h3>';

                values.description && (str += '<p class=\'tip-description\'>' + Ext.htmlEncode(Ext.htmlEncode(values.description)) +'</p><br>');
                
                if (values.isStack) {
                    return str + '</div>"';
                }
                else { 
                    // If we have groups, display a groups listing in the tooltip.
                    if (values.groups && values.groups.length > 0) {
                        var groupStr = '';
                        for (var i = -1; ++i < values.groups.length;) {
                            // Only display groups that are not stack defaults.
                            groupStr += Ext.htmlEncode(Ext.htmlEncode(values.groups[i].name)) + ', ';
                        }
                        // Include the group listing only if there are groups to list.
                        if (groupStr.length > 0) {
                            str = str + '<p class=\'group\'><label>Group(s): </label>';
                            groupStr = groupStr.substring(0, groupStr.length - 2);
                            str = str + groupStr + '</p>';
                        }
                    } 
                    var finish ; 
                    var restore ; 
                    if(   ! values.createdBy ||  ! restore){
                    	finish = str + '<p class=\'created-by\'><label>Created by: </label>' + (Ext.htmlEncode(Ext.htmlEncode("")) || '') + '</p>' +
                        '<p class=\'last-updated\'><label>Last Modified: </label>' + (values.prettyEditedDate || '') + '</p></div>"';
                    	return finish ;
                    };
                    finish =  str + '<p class=\'created-by\'><label>Created by: </label>' + (Ext.htmlEncode(Ext.htmlEncode(values.createdBy.userRealName)) || '') + '</p>' +
                           '<p class=\'last-updated\'><label>Last Modified: </label>' + (values.prettyEditedDate || '') + '</p></div>"';
                    return finish ; 
                }
            },
            
            getActions: function (values) {
                return '<ul class="dashboard-actions hide">'+
                            '<li class="share icon-share" tabindex="0" data-qtip="Share"></li>'+
                            '<li class="restore icon-refresh" tabindex="0" data-qtip="Restore"></li>'+
                            '<li class="edit icon-edit" tabindex="0" data-qtip="Edit"></li>'+
                            '<li class="delete icon-remove" tabindex="0" data-qtip="Delete"></li>'+
                        '</ul>'
            },
            encodeAndEllipsize: function(str) {
                //html encode the result since ellipses are special characters
                return Ext.util.Format.htmlEncode(
                    Ext.Array.map (
                        //get an array containing the first word of rowData.name as one elem, and the rest of name as another
                        Ext.Array.erase (/^([\S]+)\s*(.*)?/.exec(Ext.String.trim(str)), 0, 1),
                        function(it) {
                            //for each elem in the array, truncate it with an ellipsis if it is longer than 11 characters
                            return Ext.util.Format.ellipsis(it, 14);
                        }
                    //join the array back together with spaces
                    ).join(' ')
                );
            }
        });

        me.on('afterrender', function (cmp) {
            me.tpl.overwrite( cmp.body, stackOrDashboards );
            Ext.DomHelper.append( cmp.el, 
            '<ul class="actions">'+
                '<li class="manage" tabindex="0" data-qtitle="Manage" data-qtip="Activates the Share, Restore, Edit and Delete manager buttons.">Manage</li>'+
                '<li class="create" tabindex="0" data-qtitle="Create Dashboard" data-qtip="Name, describe and design a new dashboard.">+</li>'+
            '</ul>');

            me.bindEvents(cmp);
        });

        me.on('beforeclose', me.onClose, me);
        me.on('show', me.updateWindowSize, me);
        me.on('show', me.initCircularFocus, me, {single: true});
        me.on('show', me.focusActiveDashboard, me);
    }, // end initComponent

    bindEvents: function () {
        var me = this,
            $ = jQuery,
            $dom = $(me.el.dom);

        $dom
            .on('click', '.dashboard', $.proxy(me.onDashboardClick, me))
            .on('click', '.manage', $.proxy(me.toggleManage, me))
            .on('click', '.create', $.proxy(me.createDashboard, me))
            .on('mouseover', '.dashboard', $.proxy(me.onMouseOver, me))
            .on('focus', '.dashboard', $.proxy(me.onMouseOver, me))
            .on('click', '.dashboard .restore', $.proxy(me.restoreDashboard, me))
            .on('click', '.dashboard .share', $.proxy(me.shareDashboard, me))
            .on('click', '.dashboard .edit', $.proxy(me.editDashboard, me))
            .on('click', '.dashboard .delete', $.proxy(me.deleteDashboard, me));

        me.initKeyboardNav();


        // drag and drop
        var $draggedItem,
            $draggedItemParent,
            $dragProxy;

        // disable selection while dragging
        $dom
            .attr('unselectable', 'on')
            .css('user-select', 'none')
            .on('selectstart', false);

        // reorder dashboards
        $dom.on('mousedown', '.dashboard', function (evt) {
            $draggedItem = $(this);
            $draggedItemParent = $draggedItem.parent();

            $dragProxy = $draggedItem.clone().addClass('x-dd-drag-proxy drag-proxy');
            $('ul, .dashboard-name', $dragProxy).remove();
            $(document.body).append($dragProxy);

            // prevent tooltips from showing while drag n drop
            $dom.on('mouseover.reorder', '.dashboard', function (evt) { 
                evt.preventDefault();
                evt.stopPropagation();
            });

            $(document).on('mousemove.reorder', function (evt) { 
                var pageX = evt.pageX,      // The mouse position relative to the left edge of the document.
                    pageY = evt.pageY;      // The mouse position relative to the top edge of the document.

                $dragProxy.css({
                    left: pageX + 15,
                    top: pageY + 15
                });
            });

            $dom.one('mousemove.reorder', '.dashboard', function (evt) {
                var $el = $(this);
            });

            $dom.on('mousemove.reorder', '.dashboard', function (evt) { 
                var $el = $(this);

                // only allow reordering if parents match and 
                // prevent reordering stack dashboards outside of stack and vice versa.
                if($draggedItemParent[0] !== $el.parent()[0])
                    return;

                var pageX = evt.pageX,      // The mouse position relative to the left edge of the document.
                    pageY = evt.pageY,      // The mouse position relative to the top edge of the document.
                    offset = $el.offset(),  // The offset relative to the top left edge of the document.
                    width = $el.outerWidth();

                $el.removeClass(me.DROP_LEFT_CLS + ' ' + me.DROP_RIGHT_CLS);
                
                if( pageX <= offset.left + (width/2) ) {
                    $el.addClass(me.DROP_LEFT_CLS);
                }
                else {
                    $el.addClass(me.DROP_RIGHT_CLS);
                }
            });

            $dom.on('mouseleave.reorder', '.dashboard', function (evt) {
                $(this).removeClass(me.DROP_LEFT_CLS + ' ' + me.DROP_RIGHT_CLS);
            });

            // drop performed on a dashboard
            $dom.on('mouseup.reorder', '.dashboard', function (evt) {
                me._dropOnDashboard($draggedItem, $(this));
            });
         
            // cleanup on mouseup
            $(document).on('mouseup.reorder', function (evt) {
                $draggedItem =  null;
                $draggedItemParent = null;
                $dragProxy.remove();

                $(document).off('.reorder');
                $dom.off('.reorder');
            });
        });
    },

    initKeyboardNav: function () {
        var me = this;

        function move ($el, $moveToEl) {
            me._dropOnDashboard($el, $moveToEl);
        }

        function moveLeft () {
            // move item left
            var $this = $(this),
                $prev = $this.prev(),
                promise;

            if($prev.length === 1 && !$prev.hasClass('dashboard')) {
                $prev = $prev.prev();
            }

            if($prev.length === 0)
                return;

            $prev.addClass( me.DROP_LEFT_CLS );
            move($this, $prev);
        }

        function moveRight () {
            // move item right
            var $this = $(this),
                $next = $this.next(),
                promise;
            
            if($next.length === 1 && !$next.hasClass('dashboard')) {
                $next = $next.next();
            }

            if($next.length === 0)
                return;

            $next.addClass( me.DROP_RIGHT_CLS );
            move($this, $next);
        }

        $(me.el.dom)
            .on('keyup', '.dashboard', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.onDashboardClick(evt);
                }
                //left bracket
                else if (evt.which == 219) {
                    moveLeft.call(this);
                }
                //right bracket
                else if (evt.which == 221) {
                    moveRight.call(this);
                }
            })
            .on('focus', '.dashboard', function (evt) {
                $(evt.currentTarget).addClass(me.selectedItemCls);
            })
            .on('blur', '.dashboard', function (evt) {
                me._previouslyFocusedStackOrDashboard = $(evt.currentTarget).removeClass(me.selectedItemCls);
            })
            .on('focus', '.dashboard-actions li', function (evt) {
                $(evt.currentTarget).addClass('hover');
            })
            .on('blur', '.dashboard-actions li', function (evt) {
                $(evt.currentTarget).removeClass('hover');
            })
            .on('keyup', '.dashboard-actions .restore', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.restoreDashboard(evt);
                }
            })
            .on('keyup', '.dashboard-actions .share', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.shareDashboard(evt);
                }
            })
            .on('keyup', '.dashboard-actions .edit', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.editDashboard(evt);
                }
            })
            .on('keyup', '.dashboard-actions .delete', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.deleteDashboard(evt);
                }
            })
            .on('focus', '.manage', function (evt) {
                $(evt.currentTarget).addClass('selected');
            })
            .on('blur', '.manage', function (evt) {
                if(!me._managing) {
                    $(evt.currentTarget).removeClass('selected');
                }
            })
            .on('focus', '.create', function (evt) {
                $(evt.currentTarget).addClass('selected');
            })
            .on('blur', '.create', function (evt) {
                $(evt.currentTarget).removeClass('selected');
            })
            .on('keyup', '.manage', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.toggleManage(evt);
                    $(evt.currentTarget).addClass('selected');
                }
            })
            .on('keyup', '.create', function (evt) {
                if(evt.which === Ext.EventObject.ENTER) {
                    me.createDashboard(evt);
                }
            });
    },


    _dropOnDashboard: function ($draggedItem, $dashboard) {
        var me = this,
            dashboard = me.getDashboard( $dashboard ),
            draggedItem;
        
        // dropped on the same element
        if($dashboard[0] === $draggedItem[0]) {
            $dashboard.removeClass(me.DROP_LEFT_CLS + ' ' + me.DROP_RIGHT_CLS);
            return;
        }

        var droppedLeft = $dashboard.hasClass(me.DROP_LEFT_CLS);
        var store = me.dashboardStore, newIndex, oldIndex;

        oldIndex = $draggedItem.index();

        if ( droppedLeft ) {
            $dashboard.removeClass(me.DROP_LEFT_CLS);
            $draggedItem.insertBefore( $dashboard );
        }
        else {
            $dashboard.removeClass(me.DROP_RIGHT_CLS);
            $draggedItem.insertAfter( $dashboard );
        }

        newIndex = $draggedItem.index();

        //console.log(oldIndex, newIndex);

        // dropping dashboard on a dashboard
        if( $draggedItem.hasClass('dashboard') ) {
            draggedItem = me.getDashboard( $draggedItem );

            store.remove(draggedItem.model, true);

            var index = store.indexOf(dashboard.model);
            
            if ( !droppedLeft ) {
                index++;
            }

            store.insert(index, draggedItem.model);
        }

        $draggedItem.focus();
        me.initCircularFocus();
        me.reordered = true;
    },

    initCircularFocus: function () {
        var firstEl = this.body.first(),
            addBtnEl = this.el.last().last();

        this.tearDownCircularFocus();
        this.setupFocus(firstEl, addBtnEl);
    },

    focusActiveDashboard: function () {
        var me = this,
            activeDashboardId = this.activeDashboard.id,
            selectedEl = $('#dashboard'+activeDashboardId);

        setTimeout(function () {
            selectedEl && selectedEl.focus();
        }, 500);
    },

    getDashboard: function ($el) {
        return this.dashboards[ $el.attr('data-dashboard-id') ];
    },

    getElByClassFromEvent: function (evt, cls) {
        var $dashboard = $(evt.currentTarget || evt.target);
        return $dashboard.hasClass('cls') ? $dashboard : $dashboard.parents('.' + cls);
    },

    onDashboardClick: function (evt) {
        if((evt.type !== 'click' && evt.which !== Ext.EventObject.ENTER) || this._managing === true)
            return;

        var $clickedDashboard = $(evt.currentTarget),
            dashboard = this.getDashboard( $clickedDashboard );
            
        this.activateDashboard(dashboard.guid);
        
        $clickedDashboard.addClass( this.selectedItemCls );

        if( this._$lastClickedDashboard ) {
            this._$lastClickedDashboard.removeClass( this.selectedItemCls );
        }

        this._$lastClickedDashboard = $clickedDashboard;
    },

    onMouseOver: function (evt) {
        var el,
            $ = jQuery;

        if( !this._managing )
            return;

        el = $(evt.currentTarget);

        if(this._lastManageEl) {
            if(el[0] === this._lastManageEl[0]) {
                return;
            }
            else {
                $('ul', this._lastManageEl).addClass('hide');
            }
        }

        this._lastManageEl = el;

        //$('ul', el).slideDown();
        $('ul', this._lastManageEl).removeClass('hide');

        $('.dashboard', this.el.dom).css('height', el.height() + 'px');
    },

    updateDashboardEl: function ($dashboard, dashboard) {
        var $el = $(this.tpl.apply([dashboard])).insertBefore($dashboard);
        $dashboard.remove();
        $el.focus();
    },

    toggleManage: function (evt) {
        var $manageBtn;

        if(evt) {
            $manageBtn = $(evt.currentTarget);
            this.$manageBtn = $manageBtn;
        }

        if( this._managing ) {
            this.resetManage();
            this._managing = false;
        }
        else {
            // add selected class to manage button
            $manageBtn && $manageBtn.addClass('selected');
            this._managing = true;
            if(this._previouslyFocusedStackOrDashboard) {
                this._previouslyFocusedStackOrDashboard.trigger('mouseover');
            }
        }
    },

    resetManage: function () {
        if(!this._managing)
            return;

        this.$manageBtn.removeClass('selected');
        // reset the height to normal
        $('.dashboard', this.el.dom).css('height', '');
        
        // hide action buttons of previously clicked stack
        if( this._lastManageEl ) {
             //$('ul', this._lastManageEl).slideUp();
             $('ul', this._lastManageEl).addClass('hide');
             this._lastManageEl = null;
        }
        this._managing = false;
    },

    restoreDashboard: function (evt) {
        evt.stopPropagation();
        var me = this,
            $dashboard = this.getElByClassFromEvent(evt, 'dashboard'),
            dashboard = this.getDashboard($dashboard),
            dashboardGuid = dashboard.guid;

        this.warn('This action will return the dashboard <span class="heading-bold">' + Ext.htmlEncode(dashboard.name) + '</span> to its current default state. If an administrator changed the dashboard after it was assigned to you, the default state may differ from the one that originally appeared in your Switcher.', function () {
            Ext.Ajax.request({
                url: Ozone.util.contextPath() + '/dashboard/restore',
                params: {
                    guid: dashboardGuid,
                    isdefault: dashboardGuid == me.activeDashboard.guid
                },
                success: function(response, opts) {
                    var json = Ext.decode(response.responseText);
                    if (json != null && json.data != null && json.data.length > 0) {
                        me.notify('Restore Dashboard', '<span class="heading-bold">' + Ext.htmlEncode(dashboard.name) + '</span> is restored successfully to its default state!');

                        var name = json.data[0].name,
                            description = json.data[0].description;

                        dashboard.model.set({
                            'name': name,
                            'description': description
                        });
                        dashboard.name = name;
                        dashboard.description = name;

                        me.updateDashboardEl($dashboard, dashboard);

                        me.reloadDashboards = true;
                    }
                },
                failure: function(response, opts) {
                    Ozone.Msg.alert('Dashboard Manager', "Error restoring dashboard.", function() {
                        Ext.defer(function() {
                            $dashboard[0].focus();
                        }, 200, me);
                    }, me, null, me.dashboardContainer.modalWindowManager);
                    return;
                }
            });
        }, function () {
            evt.currentTarget.focus();
        });
    },

    shareDashboard: function (evt) {
        evt.stopPropagation();

        var $dashboard = this.getElByClassFromEvent(evt, 'dashboard'),
            dashboardGuid = this.getDashboard($dashboard).guid,
            dashboardIndex = this.dashboardContainer.dashboardStore.find('guid', dashboardGuid),
            dashboard = this.dashboardContainer.dashboardStore.getAt(dashboardIndex).data,
            dashboardModel = dashboard.model;

        //If exporting the current dashboard, regenerate the json to ensure changes
        //not yet pushed to the server are in the exported json
        if(this.dashboardContainer.activeDashboard.configRecord.data.guid === dashboard.guid) {
            dashboard = this.dashboardContainer.activeDashboard.getJson();
        }

        // delete model before cloning to remove circular refs
        delete dashboard.model;
        var cloneDashboard = Ozone.util.cloneDashboard(dashboard, false, true);

        // reset dashboard model
        dashboard.model = dashboardModel;

        // Stop unload event from firing long enough to submit form.
        // Have to do this because the form submit triggers the window's unload event
        // which causes competing requests.  (SEE OWF-4280)
        Ext.EventManager.un(window, 'beforeunload', this.dashboardContainer.onBeforeUnload);

        var elForm = document.createElement('form');
        var elInput = document.createElement('input');
        elInput.id = 'json';
        elInput.name = 'json';
        elInput.type = 'hidden';
        elInput.value = Ext.JSON.encode(cloneDashboard);
        elForm.appendChild(elInput);
        elForm.action = Ozone.util.contextPath() + '/servlet/ExportServlet';
        elForm.method = 'POST';
        elForm.enctype = elForm.encoding = 'multipart/form-data';
        document.body.appendChild(elForm);
        elForm.submit();
        document.body.removeChild(elForm);
        elForm = null;
        elInput = null;
        var dmScope = this;
        setTimeout(function() {
            Ext.EventManager.on(window, 'beforeunload', dmScope.dashboardContainer.onBeforeUnload, dmScope.dashboardContainer);
        }, 100);
    },

    createDashboard: function (evt) {
        var me = this,
            createDashWindow = Ext.widget('createdashboardwindow', {
            itemId: 'createDashWindow',
            dashboardContainer: me.dashboardContainer,
            ownerCt: me.dashboardContainer
        });

        createDashWindow.show();
        me.close();
    },

    editDashboard: function (evt) {
        evt.stopPropagation();

        var me = this,
            $dashboard = this.getElByClassFromEvent(evt, 'dashboard'),
            dashboard = this.getDashboard($dashboard);

        var editDashWindow = Ext.widget('createdashboardwindow', {
            itemId: 'editDashWindow',
            title: 'Edit Dashboard',
            height: 250,
            dashboardContainer: this.dashboardContainer,
            ownerCt: this.dashboardContainer,
            hideViewSelectRadio: true,
            existingDashboardRecord: dashboard.model
       }).show();

       this.close();
    },

    deleteDashboard : function(evt) {
		evt.stopPropagation();
	
		var me = this, $dashboard = this.getElByClassFromEvent(
				evt, 'dashboard'), dashboard = this
				.getDashboard($dashboard), msg;
	
		function focusEl() {
			evt.currentTarget.focus();
		}
	
		if (dashboard.groups && dashboard.groups.length > 0) {
			this.warn('Users cannot remove dashboards assigned to a group. Please contact your administrator.', focusEl);
			return;
		}
		
		if (me.dashboardStore.data.length == 1) {
			this.warn('You cannot delete - Users must have one or more dashboards', focusEl);
			return;
		}
	
		msg = 'This action will permanently delete <span class="heading-bold">' + Ext.htmlEncode(dashboard.name) + '</span>.';
	
		this.warn(msg, function() {
			me.dashboardStore.remove(dashboard.model);
			me.dashboardStore.save();
			me.notify('Delete Dashboard',
					'<span class="heading-bold">'
							+ Ext.htmlEncode(dashboard.name)
							+ '</span> deleted!');
	
			me._deletedStackOrDashboards.push(dashboard);
			me.reloadDashboards = true;
	
			var $prev = $dashboard.prev();
			$dashboard.remove();
			$prev.focus();
	
		}, focusEl);
	},

    warn: function (msg, okFn, cancelFn) {
        Ext.widget('alertwindow',{
            title: "Warning",
            html:  msg,
            minHeight: 115,
            width: 250,
            dashboardContainer: this.dashboardContainer,
            okFn: okFn,
            cancelFn: cancelFn,
            showCancelButton: !!cancelFn
        }).show();
    },

    notify: function  (title, msg, type /* default is success*/) {
        var stack_bottomright = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};
        $.pnotify({
            title: title,
            text: msg,
            type: type || 'success',
            addclass: "stack-bottomright",
            stack: stack_bottomright,
            history: false,
            sticker: false,
            icon: false,
            delay: 3000
        });
    },

    activateDashboard: function (guid) {
        this.close();
        this.dashboardContainer.activateDashboard(guid, false);
    },

    updateWindowSize: function() {
        var newWidth,
            newHeight,
            item = this.body.first().dom;
        
        if(!item)
            return;

        var itemEl = Ext.get(item),
            windowEl = this.getEl(),
            widthMargin = itemEl.getMargin('lr'),
            heightMargin = itemEl.getMargin('tb'),
            totalDashboards = this.body.query('> .dashboard').length,
            dashboardInRow = 0;

        this.dashboardItemWidth = itemEl.getWidth();
        this.dashboardItemHeight = itemEl.getHeight();

        if(totalDashboards < this.minDashboardsWidth) {
            dashboardInRow = this.minDashboardsWidth;
        }
        else if (totalDashboards > this.maxDashboardsWidth) {
            dashboardInRow = this.maxDashboardsWidth;
        }
        else {
            dashboardInRow = totalDashboards;
        }

        newWidth = (this.dashboardItemWidth + widthMargin + 1) * dashboardInRow;

        if(totalDashboards > this.maxDashboardsWidth * this.maxDashboardsHeight) {
            // add 30 to accomodate for scrollbar
            newWidth += 30;
        }
        if(totalDashboards > this.maxDashboardsWidth * this.maxDashboardsHeight) {
            newHeight = (this.dashboardItemHeight + heightMargin) * this.maxDashboardsHeight;
        }

        this.body.setSize(newWidth + 30, newHeight);
        
        this.body.setStyle({
            'max-height': ((this.dashboardItemHeight + heightMargin + 1) * this.maxDashboardsHeight) + 40 + 'px'
        });
    },

    saveDashboardOrder: function () {
        var dfd = $.Deferred();
        var gridData = this.dashboardStore.data.items;
        var viewsToUpdate = [];
        var viewGuidsToDelete = [];
    
        for (var i = 0; i < gridData.length; i++) {
            if (!gridData[i].data.removed) {
                viewsToUpdate.push({
                    guid: gridData[i].data.guid,
                    isdefault: gridData[i].data.isdefault,
                    name: gridData[i].data.name.replace(new RegExp(Ozone.lang.regexLeadingTailingSpaceChars), '')
                });
            } else {
                viewGuidsToDelete.push(gridData[i].data.guid);
            }
        }

        Ozone.pref.PrefServer.updateAndDeleteDashboards({
            viewsToUpdate: viewsToUpdate,
            viewGuidsToDelete: viewGuidsToDelete,
            updateOrder: true,
            onSuccess: function() {
                dfd.resolve();
            },
            onFailure: function() {
                dfd.reject();
            }
        });

        return dfd.promise();
    },

    onClose: function() {
        var me = this;

        me.resetManage();
        //me.tearDownCircularFocus();

        // refresh if user deleted all dashboards
        if(me.dashboardContainer.dashboardStore.getCount() === 0) {
            window.location.reload();
            return;
        }

        if (me.reordered) {
            if(me.reloadDashboards) {
                me.saveDashboardOrder().always(function () {
                    me.dashboardContainer.reloadDashboards();
                });
            }
            else {
                me.saveDashboardOrder().fail(function () {
                    me.dashboardContainer.reloadDashboards();
                });
            }
        }
        else if(me.reloadDashboards === true) {
            me.dashboardContainer.reloadDashboards();
        }
    },

    destroy: function () {
        this.tearDownCircularFocus();

        // remove jQuery listeners
        $(this.el.dom).off();

        // destroy view so that it will be recreated when opened next setTimeout
        return this.callParent();
    }
});
