var addPublishedPoint;
var addAllInView;

var togglePublishedPoint;
var removePublishedPoint;
var enableAllInView;
var disableAllInView;
var removeAllInView;
/* For reference in main publisher edit page */
var savePublisherImpl;

var syncDate,syncTime;
var uiSetup = false;

require([
         'dojo/_base/declare',
         'dojo/store/Observable',
         'dojo/store/Memory',
         'dstore/legacy/DstoreAdapter',
         'dgrid/OnDemandGrid', 
         'dgrid/editor',
         'dgrid/Keyboard',
         'put-selector/put',
         'dijit/form/Select',
         'dgrid/extensions/DijitRegistry',
         "dojo/dom", "dojo/dom-style",
         "dijit/form/DateTextBox",
         "dijit/form/TimeTextBox",
         'dojo/domReady!'
   ], function (declare, Observable, Memory, DstoreAdapter, 
		   OnDemandGrid, editor, Keyboard, put, Select, DijitRegistry, 
		   dom, style, DateTextBox, TimeTextBox) {
  	
	console.time('loading');
  	
	/* Refresh the log level */
    logLevelChanged();
	
    /* Define the save publisher impl */
    savePublisherImpl = function(name, xid, enabled, cacheWarningSize, cacheDiscardSize, publishType, sendSnapshot,
            snapshotSendPeriods, snapshotSendPeriodType) {
        // Clear messages.
        hide("hostMsg");
        hide("portMsg");
        hide("userMsg");
        hide("passMsg");

        TCPPublisherEditDwr.saveSender(name, xid, enabled, $get("port"), cacheWarningSize, cacheDiscardSize,
            publishType, sendSnapshot, snapshotSendPeriods, snapshotSendPeriodType, function(response){
    
      	  	//Enable the synce button
      	  	if(!response.hasMessages){
      	  		$set('publisherId', response.id);
      	  		enableAllButtons();
      	  		//Setup UI if not already setup and we have a new publisher
      	  		if(typeof(setupUI) === "function" && response.data.id !== -1)
      	  			setupUI();
      	  	}
      	  	
      	  	savePublisherCB(response);
      	  });
    };
    
    //If we are not a saved publisher don't build the UI yet
    if($get('publisherId') === "-1"){
    	enableAllButtons();
    	return;
    }
    
    //If so set it up now
    setupUI();

	//Helper Functions For Page
  	function queryStore(filter, store){
  		//Build the query
  		var newQuery = {};
  		for(prop in filter){
  			if(prop === 'id')
  				continue;
  			//Special case for boolean
  			if(prop === 'dataPointEnabled'){
  				switch(filter[prop]){
  				case 'enabled':
  					newQuery.dataPointEnabled = true;
  					break;
  				case 'disabled':
  					newQuery.dataPointEnabled = false;
  					break;
  				case 'none':
  				default:
  					break;
  				}
  			}else{
  				if((filter[prop] !== "")&&(filter[prop] != null))
  					newQuery[prop] = new RegExp("^.*" + filter[prop] + ".*$");
  			}
  		}
  		
  		return store.query(newQuery);
  	}
  	
  	
  	
  	/**
  	 * Helper to enable the buttons once the page is loaded
  	 */
  	function enableAllButtons(){
  		if($get('publisherId') != "-1"){
//	  		enable('setSyncBtn');
	  		enable('addAllInView');
	  		enable('removeAllInView');
  		}
  		enable('saveBtn');
  	};
  	/**
  	 * Show Loading Graphic
  	 */
  	function showLoading(id){
  		
  		var loading = dom.byId(id + 'LoadingOverlay');
  		if(loading !== null){
  			style.set(loading, {
	  				'z-index': '1002',
	  				background: "url('/images/throbber.gif') no-repeat 32px 32px",
	  				'background-position': 'center'
  			});
  		}
  		
  		loading = dom.byId(id + 'BaseLoadingOverlay');
  		if(loading !== null){
  	  		style.set(loading, {
  	  				'z-index': '1001',
  	  				opacity: '.5'
  	  			});
  	  	}
  	}

  	/**
  	 * Hide Loading Graphic
  	 */
  	function hideLoading(id){
  		var loading = dom.byId(id + 'LoadingOverlay');
  		if(loading !== null){
  			style.set(loading, {
	  				'z-index': '-1',
	  				background: ''
	  			});
  		}
  		
  		loading = dom.byId(id + 'BaseLoadingOverlay');
  		if(loading !== null){
  			style.set(loading, {
	  				'z-index': '-1',
	  				opacity: ''
	  			});
  		}
  	}
    
    function setupUI(){
    	
    	if(setupUI === true)
    		return;
    	setupUI = true;
    	
    	//Define the methods we require
    	/* Point Edit Functions */
      	addPublishedPoint = function(id){
      		var point = allPointsGrid.get('store').get(id);
      		if(!confirm(mangoTranslate('publisherEdit.vzdb.addConfirm', ["'" + point.dataPointDeviceName + ' - ' + point.dataPointName + "'"])))
      			return;
      		showMessage('addedPointsMessage');
      		showLoading('allPoints');
      		showLoading('addedPoints');
      		var addedPointsStore = addedPointsGrid.get('store');
      		var allPointsStore = allPointsGrid.get('store');
      		
      		TCPPublisherEditDwr.addPublishedPoints($get('publisherId'), [point], function(response){
      			if(response.hasMessages === true){
      				showDwrMessages(response.messages);
      			}else{
      				for(var i=0; i<response.data.addedPoints.length; i++){
      					var point = response.data.addedPoints[i];
      					allPointsStore.remove(point.dataPointId);
    	  	  			addedPointsStore.add(point);
      				}
      		  		allPointsGrid.refresh();
      		  		addedPointsGrid.refresh();
    	  	  		showMessage('addedPointsMessage', mangoTranslate('publisherEdit.vzdb.dataPointAdded', [point.dataPointName]));
      			}
  		  		hideLoading('allPoints');
	  	  		hideLoading('addedPoints');
      		});
      		
      	};
      	/**
      	 * Add all points from the all data points view based on the applied query
      	 */
      	addAllInView = function(){
      		if(!confirm(mangoTranslate('publisherEdit.vzdb.addAllConfirm')))
      			return;
      		showMessage('addedPointsMessage');
      		disable('addAllInView');
      		showLoading('allPoints');
      		showLoading('addedPoints');
      		var addedPointsStore = addedPointsGrid.get('store');
      		var allPointsStore = allPointsGrid.get('store');
      		var allInView = queryStore(allPointsFilter.get('store').get(0), allPointsStore);

      		TCPPublisherEditDwr.addPublishedPoints($get('publisherId'), allInView, function(response){
      			if(response.hasMessages === true){
      				showDwrMessages(response.messages);
      			}else{
      				for(var i=0; i<response.data.addedPoints.length; i++){
      					var point = response.data.addedPoints[i];
      					allPointsStore.remove(point.dataPointId);
    	  	  			addedPointsStore.add(point);
      				}
    	  	  		allPointsGrid.refresh();
    	  	  		addedPointsGrid.refresh();
    	  	  		showMessage('addedPointsMessage', mangoTranslate('publisherEdit.vzdb.dataPointsAdded', [allInView.length]));
      			}
      			enable('addAllInView');
	  	  		hideLoading('allPoints');
	  	  		hideLoading('addedPoints');
      		});
      	};
      	
      	removePublishedPoint = function(id){
      		//Remove from added points view
    		var point = addedPointsGrid.get('store').get(id);
      		if(!confirm(mangoTranslate('publisherEdit.vzdb.removeConfirm', ["'" + point.dataPointDeviceName + ' - ' + point.dataPointName + "'"])))
      			return;
      		showMessage('addedPointsMessage');
      		showLoading('allPoints');
      		showLoading('addedPoints');
    		
      		TCPPublisherEditDwr.removePublishedPoints($get('publisherId'), [point], function(response){
      			if(response.hasMessages === true){
      				showDwrMessages(response.messages);
      			}else{
      				for(var i=0; i<response.data.removedPoints.length; i++){
      					var removed = response.data.removedPoints[i];
      					addedPointsGrid.get('store').remove(removed.dataPointId);
          				//Add to all points view
          				allPointsGrid.get('store').add(removed);
      				}
      				
      		  		allPointsGrid.refresh();
      		  		addedPointsGrid.refresh();
    	  	  		showMessage('addedPointsMessage', mangoTranslate('publisherEdit.vzdb.dataPointRemoved', [point.dataPointName]));
      		  	}
  		  		hideLoading('allPoints');
  		  		hideLoading('addedPoints');
      		});
      	};
      	removeAllInView = function(){
      		if(!confirm(mangoTranslate('publisherEdit.vzdb.removeAllConfirm')))
      			return;
      		showMessage('addedPointsMessage');
      		disable('removeAllInView');
      		showLoading('allPoints');
      		showLoading('addedPoints');
      		var addedPointsStore = addedPointsGrid.get('store');
      		var allPointsStore = allPointsGrid.get('store');
      		var allInView = queryStore(addedPointsFilter.get('store').get(0), addedPointsStore);

      		TCPPublisherEditDwr.removePublishedPoints($get('publisherId'), allInView, function(response){
      			if(response.hasMessages === true){
      				showDwrMessages(response.messages);
      			}else{
      				for(var i=0; i<response.data.removedPoints.length; i++){
      					var removed = response.data.removedPoints[i];
      					addedPointsGrid.get('store').remove(removed.dataPointId);
          				//Add to all points view
          				allPointsGrid.get('store').add(removed);
      				}
      		  		allPointsGrid.refresh();
      		  		addedPointsGrid.refresh();
    	  	  		showMessage('addedPointsMessage', mangoTranslate('publisherEdit.vzdb.dataPointsRemoved', [allInView.length]));
      		  	}
  		  		hideLoading('allPoints');
  		  		hideLoading('addedPoints');
      	  		enable('removeAllInView');
      		});
      	}
    	
    	
    	//Show the div we build
      	show("publisherPointsEditDiv");
      	
    	//Setup the Added Points Filter
      	var allPointsFilterData = [
      		{
      			id: 0,
      			dataPointXid: null,
      			dataPointName: null,
      			dataPointDeviceName: null,
      			dataPointType: null,
      			dataPointEnabled: 'none',
      		}	
      	];
      	var allPointsFilter = new declare(([OnDemandGrid, Keyboard, DijitRegistry]))({
            store: new Memory({data: allPointsFilterData}),
            columns: [
                editor({
                  	label: mangoTranslate('common.xid'),
                  	field: 'dataPointXid',
                  	editor: 'text',
                  	autoSave: true,
                	sortable: false
                }),
                editor({
                	label: mangoTranslate('common.name'),
                	field: 'dataPointName',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({ 
                	label: mangoTranslate('common.deviceName'),
                	field: 'dataPointDeviceName',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({ 
                	label: mangoTranslate('common.type'),
                	field: 'dataPointType',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({
                	label:   mangoTranslate('common.enabled'),
                	field: 'dataPointEnabled',
                	className: '.dgrid-column-dataPointEnabled',
                	editorArgs: {
                        style: "width:35px;border: 1px solid green;",
                        options: [
                                  {label: mangoTranslate('common.all'), value: 'none'},
                                  {label: mangoTranslate('common.enabled'), value: 'enabled'},
                                  {label: mangoTranslate('common.disabled'), value: 'disabled'}
                                  ]
                    },
                	autoSave: true,
                	sortable: false,
                	renderCell: function(object, value, node, options) {
                  	   var span = put('span');
                  	   var title,src;
                  	   if(value === 'enabled'){
                  		   src = '/images/database_go.png';
                  		   title = 'enabled';
                      	   var img = put(span, 'img[src=$][title=$]', src, title);
                      	   return span;
                  	   }else if(value === 'disabled'){
                  		   src = '/images/database_stop.png';
                  		   title = 'disabled';
                      	   var img = put(span, 'img[src=$][title=$]', src, title);
                      	   return span;
                  	   }else{
                  		 return node.innerHTML = mangoTranslate('common.all');
                  	   }
                     }
                }, Select, 'click'),
                {	
                	className: '.dgrid-column-buttons',
                	label: ' ',
                	renderCell: function(object, value, node, options) {
                		return put('span');
                	}
                	
                }
            ]
        }, 'allPointsFilter');
      	allPointsFilter.on('dgrid-datachange', function(event){
      		
      		//Build the query
      		var newQuery = {};
      		var filter = allPointsFilter.store.get(0);
      		for(prop in filter){
      			if(prop === 'id')
      				continue;
      			//Special case for boolean
      			if(prop === 'dataPointEnabled'){
    	  			//Since this won't be in the filter store yet
    	  			if(prop === event.cell.column.field){
    	  				switch(event.value){
    	  				case 'enabled':
    	  					newQuery.dataPointEnabled = true;
    	  					break;
    	  				case 'disabled':
    	  					newQuery.dataPointEnabled = false;
    	  					break;
    	  				case 'none':
    	  				default:
    	  					break;
    	  				}
    	  			}else{
    	  				switch(filter[prop]){
    	  				case 'enabled':
    	  					newQuery.dataPointEnabled = true;
    	  					break;
    	  				case 'disabled':
    	  					newQuery.dataPointEnabled = false;
    	  					break;
    	  				case 'none':
    	  				default:
    	  					break;
    	  				}
    	  			}

      			}else{
      				try{
	    	  			//Since this won't be in the filter store yet
	    	  			if(prop === event.cell.column.field){
	    	  				if((event.value !== "")&&(event.value !== null))
	    	  					newQuery[prop] = new RegExp("^.*" + event.value + ".*$");
	    	  			}else{
	    	  				if((filter[prop] !== "")&&(filter[prop] != null))
	    	  					newQuery[prop] = new RegExp("^.*" + filter[prop] + ".*$");
	    	  			}
      				}catch(err){
      					showMessage('allPointsError', err.message);
      					return;
      				}
      			}
      		}
      		
      		//Apply to published points grid
      		allPointsGrid.set('query', newQuery);
      	});
      	
      	//Setup the All Points Grid
      	var allPointsGrid = new OnDemandGrid({
           store: null,
           loadingMessage: mangoTranslate('publisherEdit.loadingPoints'),
           noDataMessage: mangoTranslate('publisherEdit.noPoints'),
           columns: {
        	   dataPointXid: mangoTranslate('common.xid'),
               dataPointName: mangoTranslate('common.name'),
               dataPointDeviceName: mangoTranslate('common.deviceName'),
               dataPointType: mangoTranslate('common.type'),
               dataPointEnabled: {
            	   label: '',
            	   renderCell: function(object, value, node, options) {
                	   var span = put('span');
                	   var title,src;
                	   if(object.dataPointEnabled === true){
                		   title = 'enabled';
                		   src = '/images/database_go.png';
                	   }else{
                		   title = 'disabled';
                		   src = '/images/database_stop.png';
                	   }
                	   var img = put(span, 'img[src=$][title=$]', src, title);
                	   return span;
                   }
               },
               buttons: {
                   label: ' ',
                   sortable: false,
                   resizeable: false,
                   renderHeaderCell: function(th){
                	   return;
                   },
                   renderCell: function(object, value, node, options) {
                	   
                	   var elementId = "addPublishedPoint_" + object.dataPointId;
                	   var title = 'Add';
                	   var src = '/images/add.png'
                	   var action = 'addPublishedPoint(' + object.dataPointId + ');';
                	   var img = put('img.ptr#$[src=$][title=$][onclick=$]', elementId, src, title, action);
                	   return img;
                   }
               }
           }
       	}, 'allPoints');
      	allPointsGrid.on('dgrid-error', function(event){
       	    showMessage('allPointsError', event.error.message);
       	});
      	allPointsGrid.on("dgrid-refresh-complete", function(event) {
      		showMessage('allPointsError');
       	});
      	allPointsGrid.startup();
      	showLoading('allPoints');
      	
      	//Setup the Added Points Filter
      	var addedPointsFilterData = [
      		{
      			id: 0,
      			dataPointXid: null,
      			dataPointName: null,
      			dataPointDeviceName: null,
      			dataPointType: null,
      			dataPointEnabled: 'none',
      			syncedToMessage: null
      		}	
      	];
      	var addedPointsFilter = new declare(([OnDemandGrid, Keyboard, DijitRegistry]))({
            store: new Memory({data: addedPointsFilterData}),
            columns: [
				editor({
					label: mangoTranslate('common.xid'),
					field: 'dataPointXid',
					editor: 'text',
					autoSave: true,
					sortable: false
				}),                      
                editor({
                	label: mangoTranslate('common.name'),
                	field: 'dataPointName',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({ 
                	label: mangoTranslate('common.deviceName'),
                	field: 'dataPointDeviceName',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({ 
                	label: mangoTranslate('common.type'),
                	field: 'dataPointType',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                editor({
                	label: mangoTranslate('common.enabled'),
                	field: 'dataPointEnabled',
                	className: '.dgrid-column-dataPointEnabled',
                	editorArgs: {
                        style: "width:35px;border: 1px solid green;",
                        options: [
                                  {label: mangoTranslate('common.all'), value: 'none'},
                                  {label: mangoTranslate('common.enabled'), value: 'enabled'},
                                  {label: mangoTranslate('common.disabled'), value: 'disabled'}
                                  ]
                    },
                	autoSave: true,
                	sortable: false,
                	renderCell: function(object, value, node, options) {
                 	   var span = put('span');
                 	   var title,src;
                 	   if(value === 'enabled'){
                 		   src = '/images/database_go.png';
                 		   title = mangoTranslate('common.enabled');
                     	   var img = put(span, 'img[src=$][title=$]', src, title);
                     	   return span;
                 	   }else if(value === 'disabled'){
                 		   src = '/images/database_stop.png';
                 		   title = mangoTranslate('common.disabled');
                     	   var img = put(span, 'img[src=$][title=$]', src, title);
                     	   return span;
                 	   }else{
                 		   return node.innerHTML = mangoTranslate('common.all');
                 	   }
                    }
                }, Select, 'click'),
                editor({ 
                	label: mangoTranslate('publisherEdit.vzdb.message'),
                	field: 'syncedToMessage',
                	editor: 'text',
                	autoSave: true,
                	sortable: false
                }),
                {
                	className: '.dgrid-column-buttons',
                	label: ' ',
                	renderCell: function(object, value, node, options) {
                		return put('span');
                	}
                }
            ]
        }, 'addedPointsFilter');
      	addedPointsFilter.on('dgrid-datachange', function(event){
      		
      		//Build the query
      		var newQuery = {};
      		var filter = addedPointsFilter.store.get(0);
      		for(prop in filter){
      			if(prop === 'id')
      				continue;
      			//Special case for boolean
      			if(prop === 'dataPointEnabled'){
    	  			//Since this won't be in the filter store yet
    	  			if(prop === event.cell.column.field){
    	  				switch(event.value){
    	  				case 'enabled':
    	  					newQuery.dataPointEnabled = true;
    	  					break;
    	  				case 'disabled':
    	  					newQuery.dataPointEnabled = false;
    	  					break;
    	  				case 'none':
    	  				default:
    	  					break;
    	  				}
    	  			}else{
    	  				switch(filter[prop]){
    	  				case 'enabled':
    	  					newQuery.dataPointEnabled = true;
    	  					break;
    	  				case 'disabled':
    	  					newQuery.dataPointEnabled = false;
    	  					break;
    	  				case 'none':
    	  				default:
    	  					break;
    	  				}
    	  			}

      			}else{
    	  			//Since this won't be in the filter store yet
    	  			if(prop === event.cell.column.field){
    	  				if((event.value !== "")&&(event.value !== null))
    	  					newQuery[prop] = new RegExp("^.*" + event.value + ".*$");
    	  			}else{
    	  				if((filter[prop] !== "")&&(filter[prop] != null))
    	  					newQuery[prop] = new RegExp("^.*" + filter[prop] + ".*$");
    	  			}
      			}
      		}
      		
      		//Apply to published points grid
      		addedPointsGrid.set('query', newQuery);
      	});
      	
    	//Setup the Selected Points Grid
      	var addedPointsGrid = new OnDemandGrid({
           store: null,
           loadingMessage: mangoTranslate('publisherEdit.loadingPoints'),
           noDataMessage: mangoTranslate('publisherEdit.noPoints'),
           columns: {
        	   dataPointXid: mangoTranslate('common.xid'),
               dataPointName: mangoTranslate('common.name'),
               dataPointDeviceName: mangoTranslate('common.deviceName'),
               dataPointType: mangoTranslate('common.type'),
               dataPointEnabled: {
            	   label: '',
            	   renderCell: function(object, value, node, options) {
                	   var span = put('span');
                	   var title,src;
                	   if(object.dataPointEnabled === true){
                		   title = mangoTranslate('common.enabled');
                		   src = '/images/database_go.png';
                	   }else{
                		   title = mangoTranslate('common.disabled');
                		   src = '/images/database_stop.png';
                	   }
                	   var img = put(span, 'img[src=$][title=$]', src, title);
                	   return span;
                   }
               },
               syncedToMessage: mangoTranslate('publisherEdit.vzdb.message'),
               buttons: {
                   label: ' ',
                   sortable: false,
                   resizeable: false,
                   renderHeaderCell: function(th){
                	   return;
                   },
                   renderCell: function(object, value, node, options) {
                	   var elementId = "removePublishedPoint_" + object.dataPointId;
                	   var title = mangoTranslate('common.delete');
                	   var src = '/images/delete.png'
                	   var action = 'removePublishedPoint(' + object.dataPointId + ');';
                	   var img = put('img.ptr#$[src=$][title=$][onclick=$]', elementId, src, title, action);
                	   return img;
                   }
               }

           }
       	}, 'addedPoints');
      	addedPointsGrid.on('dgrid-error', function(event){
       	    showMessage('addedPointsGridMessage', event.error.message);
       	});
      	addedPointsGrid.on("dgrid-refresh-complete", function(event) {
      		showMessage('addedPointsGridMessage');
       	});
      	addedPointsGrid.startup();
      	showLoading('addedPoints');
      	
      	//Request the data
      	TCPPublisherEditDwr.initSender(function(response) {
    		
    	    //FIXME this displays the date in the browser's timezone, not ideal
    	    syncDate = new DateTextBox({
    	  	    value: response.data.currentDate,
    	  	    style: "width: 10em; color: gray",
    	    }, "syncDate");

    	    syncTime = new TimeTextBox({
    	  		value: response.data.currentDate,
    	  		style: "width: 10em; color: gray",
    	    }, "syncTime");
    		
    	   	var allPointsStore = new Memory({
       			idProperty: 'dataPointId',
       			data: response.data.unpublishedPoints
       		});
    	   	allPointsGrid.set('store', allPointsStore);
    	   	hideLoading('allPoints');
    	   	
    		var addedPointsStore = new Memory({
    			idProperty: 'dataPointId',
    	   		data: response.data.publishedPoints
    	   	});
    		addedPointsGrid.set('store', addedPointsStore);
    		hideLoading('addedPoints');

    		enableAllButtons();
 
           	console.timeEnd('loading');
    	});
      	
    }
  	
	
});