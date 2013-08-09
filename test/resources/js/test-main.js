var tests = Object.keys(window.__karma__.files).filter(function (file) {
    return /Spec\.js$/.test(file);
});

requirejs.config({
    // Karma serves files from '/base'
    baseUrl: '',

    paths: {
        'ngDefine' : '/play/sd/assets/vendor/requirejs-angular-define/ngDefine',
        'jquery' :   '/play/sd/assets/vendor/jquery/jquery-1.9.1.min',
        'angular' :  '/play/sd/assets/vendor/angular/angular',
        'angular-mocks' :  '/play/sd/assets/vendor/angular/angular-mocks'
    },

    shim: {
        'jquery' : { exports: '$' },
        'angular' : { deps: [ 'jquery' ], exports: 'angular' },
        'angular-mocks' : { deps: [ 'angular' ] },
        'ngDefine' : { deps: [ 'angular' ], exports: 'ngDefine' }
    },

    packages : [
        { name: 'disco', location: '/play/sd/app' },
        { name: 'angular-ui', location: '/play/sd/assets/vendor/angular-ui' },
        { name: 'web-common', location: '/play/sd/assets/vendor/web-common' }
    ]

});

define("sound-cloud", [], function () {
    // empty sound cloud module for tests
    return {};
});

require([ 'angular', 'angular-mocks', 'ngDefine', 'jquery'], function() {
    require(tests, function() {
        window.__karma__.start();
    });
});