'use strict';

/** Controllers 1 */
angular.module('fishstoreOne.controllers', ['fishstoreOne.services']).
controller('FishStoreOneCtrl',function ($scope, $http, $location, $timeout) {
	$scope.latest_catch_empty = JSON.parse('[]');
	$scope.latest_catch = $scope.latest_catch_empty; // this will be modified with $$hashkey
	$scope.latest_catch_raw = $scope.latest_catch_empty;
	$scope.latest_catch_size = 0;
	$scope.delivery_result_none = JSON.parse('{"message": "", "time": ""}');
	$scope.delivery_result_pending = JSON.parse('{"message": "...", "time": "..."}');
	$scope.delivery_result_error = JSON.parse('{"message": "Error", "time": ""}');
	$scope.delivery_result = $scope.delivery_result_none;
	
	$scope.catchExists = function() {
		return $scope.latest_catch_size > 0;
	}
	$scope.deliveryResultExists = function() {
		return ($scope.delivery_result.message != "" && $scope.delivery_result.message != "...");
	}
	$scope.getLatestCatch = function() {
       var url = '/fish_store_one/catch/latest';
	   $http({method: 'GET', url: url
	   }).success(function(data, status, headers, config) {
		     console.log(url);
		     console.log(data);
		     $scope.latest_catch = data;
		     $scope.latest_catch_raw = data;
		     $scope.latest_catch_size = data.length;
		     $scope.delivery_result = $scope.delivery_result_none;
	   }).error(function(data, status, headers, config) {
		     console.log('GET ' + url + ' ERROR ' + status)
		     $scope.latest_catch = $scope.latest_catch_empty;
		     $scope.latest_catch_raw = $scope.latest_catch_empty;
		     $scope.latest_catch_size = 0;
		     $scope.delivery_result = $scope.delivery_result_none;
	   });
	}
	$scope.deliverLatestCatch = function() {
		$scope.delivery_result = $scope.delivery_result_pending;
		var url = '/fish_store_one/delivery';
		$http({method: 'POST', url: url,
		   headers: {'Content-Type': 'application/json'},
		   data: JSON.stringify($scope.latest_catch_raw)
		   // data: {'consumerId': $scope.consumer_simulated.id, 'url': $scope.simulator_callback_url}
		}).success(function(data, status, headers, config) {
		      console.log(url);
		      console.log(data);
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = data;
		}).error(function(data, status, headers, config) {
		      console.log('POST ' + url + ' ERROR ' + status)
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = $scope.delivery_result_error;
	    });
	}
});


/** Controllers 2 */
angular.module('fishstoreTwo.controllers', ['fishstoreTwo.services']).
controller('FishStoreTwoCtrl',function ($scope, $http, $location, $timeout) {
	$scope.latest_catch_empty = JSON.parse('[]');
	$scope.latest_catch = $scope.latest_catch_empty; // this will be modified with $$hashkey
	$scope.latest_catch_raw = $scope.latest_catch_empty;
	$scope.latest_catch_size = 0;
	// DeliveryReceipt(id: Long, fishCount: Int, totalWeight: Double, payment: Double, time: String)
	$scope.delivery_result_none = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "", "message": ""}');
	$scope.delivery_result_pending = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "...", "message": ""}');
	$scope.delivery_result_error = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "", "message": "Error"}');
	$scope.delivery_result = $scope.delivery_result_none;
	$scope.delivery_feed_msgs = []; // array of dropped fish
	
	$scope.catchExists = function() {
		return $scope.latest_catch_size > 0;
	}
	$scope.deliveryResultExists = function() {
		return ($scope.delivery_result.message != "" && $scope.delivery_result.message != "...");
	}
	$scope.getLatestCatch = function() {
       var url = '/fish_store_two/catch/latest';
	   $http({method: 'GET', url: url
	   }).success(function(data, status, headers, config) {
		     console.log(url);
		     console.log(data);
		     $scope.latest_catch = data;
		     $scope.latest_catch_raw = data;
		     $scope.latest_catch_size = data.length;
		     $scope.delivery_result = $scope.delivery_result_none;
	   }).error(function(data, status, headers, config) {
		     console.log('GET ' + url + ' ERROR ' + status)
		     $scope.latest_catch = $scope.latest_catch_empty;
		     $scope.latest_catch_raw = $scope.latest_catch_empty;
		     $scope.latest_catch_size = 0;
		     $scope.delivery_result = $scope.delivery_result_none;
	   });
	}
	$scope.deliverLatestCatch = function() {
		$scope.delivery_result = $scope.delivery_result_pending;
		var url = '/fish_store_two/delivery';
		$http({method: 'POST', url: url,
		   headers: {'Content-Type': 'application/json'},
		   data: JSON.stringify($scope.latest_catch_raw)
		   // data: {'consumerId': $scope.consumer_simulated.id, 'url': $scope.simulator_callback_url}
		}).success(function(data, status, headers, config) {
		      console.log(url);
		      console.log(data);
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = data;
		}).error(function(data, status, headers, config) {
		      console.log('POST ' + url + ' ERROR ' + status)
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = $scope.delivery_result_error;
	    });
	}
	
	/** handle incoming delivery feed messages: add to messages array */
    $scope.addDeliveryFeedMsg = function (msg) { 
    	var msgobj = JSON.parse(msg.data);
    	console.log('Got DeliveryFeedMsg' + msg.data);
        $scope.$apply(function () { 
        	$scope.delivery_feed_msgs.push(msgobj); // add to array
        });
    };
    
	/** start listening to the deliery feed for the fish store */
    $scope.listen = function () {
    	$scope.delivery_feed = new EventSource("/feed/fish_store_two/delivery"); 
        $scope.delivery_feed.addEventListener("message", $scope.addDeliveryFeedMsg, false);
    };
    
    $scope.listen();
});



/** Controllers 3 */
angular.module('fishstoreThree.controllers', ['fishstoreThree.services']).
controller('FishStoreThreeCtrl',function ($scope, $http, $location, $timeout) {
	$scope.latest_catch_empty = JSON.parse('[]');
	$scope.latest_catch = $scope.latest_catch_empty; // this will be modified with $$hashkey
	$scope.latest_catch_raw = $scope.latest_catch_empty;
	$scope.latest_catch_size = 0;
	// DeliveryReceipt(id: Long, fishCount: Int, totalWeight: Double, payment: Double, time: String)
	$scope.delivery_result_none = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "", "message": ""}');
	$scope.delivery_result_pending = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "...", "message": ""}');
	$scope.delivery_result_error = JSON.parse('{"id": 0, "fishCount": 0, "totalWeight": 0, "payment": 0, "time": "", "message": "Error"}');
	$scope.delivery_result = $scope.delivery_result_none;
	$scope.delivery_feed_msgs = []; // array of dropped fish
	$scope.fish_catch_count_requested = 100; // input number
	
	$scope.catchExists = function() {
		return $scope.latest_catch_size > 0;
	}
	$scope.deliveryResultExists = function() {
		return ($scope.delivery_result.message != "" && $scope.delivery_result.message != "...");
	}
	$scope.getLatestCatch = function() {
       var url = '/fish_store_three/catch/latest/' + $scope.fish_catch_count_requested;
	   $http({method: 'GET', url: url
	   }).success(function(data, status, headers, config) {
		     console.log(url);
		     console.log(data);
		     $scope.latest_catch = data;
		     $scope.latest_catch_raw = data;
		     $scope.latest_catch_size = data.length;
		     $scope.delivery_result = $scope.delivery_result_none;
	   }).error(function(data, status, headers, config) {
		     console.log('GET ' + url + ' ERROR ' + status)
		     $scope.latest_catch = $scope.latest_catch_empty;
		     $scope.latest_catch_raw = $scope.latest_catch_empty;
		     $scope.latest_catch_size = 0;
		     $scope.delivery_result = $scope.delivery_result_none;
	   });
	}
	$scope.deliverLatestCatch = function() {
		$scope.delivery_result = $scope.delivery_result_pending;
		var url = '/fish_store_three/delivery';
		$http({method: 'POST', url: url,
		   headers: {'Content-Type': 'application/json'},
		   data: JSON.stringify($scope.latest_catch_raw)
		   // data: {'consumerId': $scope.consumer_simulated.id, 'url': $scope.simulator_callback_url}
		}).success(function(data, status, headers, config) {
		      console.log(url);
		      console.log(data);
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = data;
		}).error(function(data, status, headers, config) {
		      console.log('POST ' + url + ' ERROR ' + status)
		      $scope.latest_catch = $scope.latest_catch_empty;
			  $scope.latest_catch_size = 0;
			  $scope.delivery_result = $scope.delivery_result_error;
	    });
	}
	
	/** handle incoming delivery feed messages: add to messages array */
    $scope.addDeliveryFeedMsg = function (msg) { 
    	var msgobj = JSON.parse(msg.data);
    	console.log('Got DeliveryFeedMsg' + msg.data);
        $scope.$apply(function () { 
        	$scope.delivery_feed_msgs.push(msgobj); // add to array
        });
    };
    
	/** start listening to the deliery feed for the fish store */
    $scope.listen = function () {
    	$scope.delivery_feed = new EventSource("/feed/fish_store_three/delivery"); 
        $scope.delivery_feed.addEventListener("message", $scope.addDeliveryFeedMsg, false);
    };
    
    $scope.listen();
});


// WhaleSighting - breed: String, count: Int, description: String, timestamp: Long, comments: List[String]
/** Controllers 4 */
angular.module('whaleSightings.controllers', ['whaleSightings.services']).
    controller('whaleSightingsCtrl', function ($scope, $http, $location, $timeout) {

        $scope.open_sighting_empty = JSON.parse('[]'); // single sighting selection
        $scope.sightings_feed_msgs = []; // array of whale sightings
        $scope.sighting_none = JSON.parse('{"_id": "", "breed": "", "count": 0, "description": "", "timestamp": 0, "comments": []}');
        $scope.single_sighting = $scope.sighting_none;
        $scope.single_sighting_display_date = $scope.single_sighting.timestamp;
        $scope.single_sighting_display_comments = "";

        $scope.form_new_sighting = {};
        $scope.form_new_sighting.breed = "";
        $scope.form_new_sighting.count = 1;
        $scope.form_new_sighting.description = "";
        $scope.form_new_sighting_breed_options = [
             { id : "Right Whale", name: "Right Whale" }
            ,{ id : "Bowhead Whale", name: "Bowhead Whale" }
            ,{ id : "Gray Whale"  , name: "Gray Whale" }
            ,{ id : "Fin Whale"  , name: "Fin Whale" }
            ,{ id : "Sei Whale"  , name: "Sei Whale" }
            ,{ id : "Blue Whale"  , name: "Blue Whale" }
            ,{ id : "Narwhal"  , name: "Narwhal" }
            ,{ id : "Beluga"  , name: "Beluga" }
            ,{ id : "Unknown"  , name: "Unknown" }
        ];

        $scope.form_comment = {};
        $scope.form_comment.comment = "";

        $scope.singleSightingSelectedStubbTrue = function () { return true; }
        $scope.singleSightingSelected = function () {
            return ($scope.single_sighting.breed != "" && $scope.single_sighting.timestamp > 0);
        }
        $scope.singleSightingNotSelected = function () {
            return ($scope.single_sighting.breed == "" && $scope.single_sighting.timestamp == 0);
        }

        /**
         * GET sighting
         * We go back to the server to make sure we have a current comments list
         */
        $scope.selectSighting = function (_id) {
            console.log('selectSighting ' + _id);
            var url = '/whalesightings/' + _id;
            $http({method: 'GET', url: url
            }).success(function(data, status, headers, config) {
                console.log("GET success.");
                console.log(data);
                $scope.single_sighting = data;
                $scope.single_sighting_display_date = new Date($scope.single_sighting.timestamp);
                $scope.single_sighting.comments.shift();
                $scope.single_sighting_display_comments = $scope.single_sighting.comments.join(", ");
            }).error(function(data, status, headers, config) {
                console.log('GET ' + url + ' ERROR ' + status)
                // TODO error message
            });
        }

        /** POST new sighting */
        $scope.submitNewSighting = function() {
            var jBody = JSON.stringify($scope.form_new_sighting);
            var url = '/whalesightings';
            console.log('POST ' + jBody);
            $http({method: 'POST', url: url,
                headers: {'Content-Type': 'application/json'},
                data: jBody
            }).success(function (data, status, headers, config) {
                console.log("POST success.");
                console.log(data);
            }).error(function (data, status, headers, config) {
                console.log('POST ' + url + ' ERROR ' + status);
                // TODO failure message
            });
        }

        $scope.submitNewComment = function() {
            var jBody = JSON.stringify($scope.form_comment);
            var id = $scope.single_sighting._id;
            var url = '/whalesightings/' + id + '/comment';
            console.log('PUT ' + url + ' ' + jBody);
            $http({method: 'PUT', url: url,
                headers: {'Content-Type': 'application/json'},
                data: jBody
            }).success(function (data, status, headers, config) {
                console.log("PUT success.");
                $scope.selectSighting(id);
            }).error(function (data, status, headers, config) {
                console.log('POST ' + url + ' ERROR ' + status);
                // TODO failure message
            });

        }

        /** handle incoming delivery feed messages: add to messages array */
        $scope.addSightingFeedMsg = function (msg) {
            var msgobj = JSON.parse(msg.data);
            console.log('Got SightingsFeedMsg' + msg.data);
            $scope.$apply(function () {
                $scope.sightings_feed_msgs.pop(); // take off last
                $scope.sightings_feed_msgs.unshift(msgobj); // add to first index of the array

            });
        };

        $scope.loadRecent = function()  {
            var url = '/whalesightings/limit/10'
            $http({method: 'GET', url: url
            }).success(function(data, status, headers, config) {
                console.log("loadRecent GET success.");
                $scope.sightings_feed_msgs = data;
            }).error(function(data, status, headers, config) {
                console.log('GET ' + url + ' ERROR ' + status)
                // TODO error message
            });
        }

        /** start listening to the deliery feed for the fish store */
        $scope.listen = function () {
            $scope.delivery_feed = new EventSource("/feed/whalesightings/sightings");
            $scope.delivery_feed.addEventListener("message", $scope.addSightingFeedMsg, false);
        };

        $scope.loadRecent(); // on page load fetch the latest 10 sightings
        $scope.listen(); // establish event source for sightings feed

    });