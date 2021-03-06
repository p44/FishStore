'use strict';

/** app level module which depends on services and controllers */
angular.module('fishstoreOne', ['fishstoreOne.controllers', 'ngRoute']);
angular.module('fishstoreTwo', ['fishstoreTwo.controllers', 'ngRoute']);
angular.module('fishstoreThree', ['fishstoreThree.controllers', 'ngRoute']);
angular.module('whaleSightings', ['whaleSightings.controllers', 'ngRoute']);

/** services module initialization, allows adding services to module in multiple files */
angular.module('fishstoreOne.services', []);
angular.module('fishstoreTwo.services', []);
angular.module('fishstoreThree.services', []);
angular.module('whaleSightings.services', []);
