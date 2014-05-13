/*
	Copyright (c) 2004-2010, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/


if(!dojo._hasResource["dojox.charting.DataSeries"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.DataSeries"] = true;
dojo.provide("dojox.charting.DataSeries");

dojo.require("dojox.lang.functional");

dojo.declare("dojox.charting.DataSeries", null, {
	constructor: function(store, kwArgs, value){
		//	summary:
		//		Series adapter for dojo.data stores.
		//	store: Object:
		//		A dojo.data store object.
		//	kwArgs: Object:
		//		A store-specific keyword parameters used for fetching items.
		//		See dojo.data.api.Read.fetch().
		//	value: Function|Object|String|Null:
		//		Function, which takes a store, and an object handle, and
		//		produces an output possibly inspecting the store's item. Or
		//		a dictionary object, which tells what names to extract from
		//		an object and how to map them to an output. Or a string, which
		//		is a numeric field name to use for plotting. If undefined, null
		//		or empty string (the default), "value" field is extracted.
		this.store = store;
		this.kwArgs = kwArgs;

		if(value){
			if(dojo.isFunction(value)){
				this.value = value;
			}else if(dojo.isObject(value)){
				this.value = dojo.hitch(this, "_dictValue",
					dojox.lang.functional.keys(value), value);
			}else{
				this.value = dojo.hitch(this, "_fieldValue", value);
			}
		}else{
			this.value = dojo.hitch(this, "_defaultValue");
		}

		this.data = [];

		this._events = [];

		if(this.store.getFeatures()["dojo.data.api.Notification"]){
			this._events.push(
				dojo.connect(this.store, "onNew", this, "_onStoreNew"),
				dojo.connect(this.store, "onDelete", this, "_onStoreDelete"),
				dojo.connect(this.store, "onSet", this, "_onStoreSet")
			);
		}

		this.fetch();
	},

	destroy: function(){
		//	summary:
		//		Clean up before GC.
		dojo.forEach(this._events, dojo.disconnect);
	},

	setSeriesObject: function(series){
		//	summary:
		//		Sets a dojox.charting.Series object we will be working with.
		//	series: dojox.charting.Series:
		//		Our interface to the chart.
		this.series = series;
	},

	// value transformers

	_dictValue: function(keys, dict, store, item){
		var o = {};
		dojo.forEach(keys, function(key){
			o[key] = store.getValue(item, dict[key]);
		});
		return o;
	},

	_fieldValue: function(field, store, item){
		return store.getValue(item, field);
	},

	_defaultValue: function(store, item){
		return store.getValue(item, "value");
	},

	// store fetch loop

	fetch: function(){
		//	summary:
		//		Fetches data from the store and updates a chart.
		if(!this._inFlight){
			this._inFlight = true;
			var kwArgs = dojo.delegate(this.kwArgs);
			kwArgs.onComplete = dojo.hitch(this, "_onFetchComplete");
			kwArgs.onError = dojo.hitch(this, "onFetchError");
			this.store.fetch(kwArgs);
		}
	},

	_onFetchComplete: function(items, request){
		this.items = items;
		this._buildItemMap();
		this.data = dojo.map(this.items, function(item){
			return this.value(this.store, item);
		}, this);
		this._pushDataChanges();
		this._inFlight = false;
	},

	onFetchError: function(errorData, request){
		//	summary:
		//		As stub to process fetch errors. Provide so user can attach to
		//		it with dojo.connect(). See dojo.data.api.Read fetch() for
		//		details: onError property.
		this._inFlight = false;
	},

	_buildItemMap: function(){
		if(this.store.getFeatures()["dojo.data.api.Identity"]){
			var itemMap = {};
			dojo.forEach(this.items, function(item, index){
				itemMap[this.store.getIdentity(item)] = index;
			}, this);
			this.itemMap = itemMap;
		}
	},

	_pushDataChanges: function(){
		if(this.series){
			this.series.chart.updateSeries(this.series.name, this);
			this.series.chart.delayedRender();
		}
	},

	// store notification handlers

	_onStoreNew: function(){
		// the only thing we can do is to re-fetch items
		this.fetch();
	},

	_onStoreDelete: function(item){
		// we cannot do anything with deleted item, the only way is to compare
		// items for equality
		if(this.items){
			var flag = dojo.some(this.items, function(it, index){
				if(it === item){
					this.items.splice(index, 1);
					this._buildItemMap();
					this.data.splice(index, 1);
					return true;
				}
				return false;
			}, this);
			if(flag){
				this._pushDataChanges();
			}
		}
	},

	_onStoreSet: function(item){
		if(this.itemMap){
			// we can use our handy item map, if the store supports Identity
			var id = this.store.getIdentity(item), index = this.itemMap[id];
			if(typeof index == "number"){
				this.data[index] = this.value(this.store, this.items[index]);
				this._pushDataChanges();
			}
		}else{
			// otherwise we have to rely on item's equality
			if(this.items){
				var flag = dojo.some(this.items, function(it, index){
					if(it === item){
						this.data[index] = this.value(this.store, it);
						return true;
					}
					return false;
				}, this);
				if(flag){
					this._pushDataChanges();
				}
			}
		}
	}
});

}
