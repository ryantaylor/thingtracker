// var express = require( 'express' );
// var app = express();
 
// app.get( '/products', function( req, res ) {
//     res.send( [ { name: 'nonsense' }, { name: 'craziness' } ] );
//     console.log( 'processed /products request' );
// });

// app.get( '/products/:id', function( req, res ) {
//     res.send( { id: req.params.id, name: 'name', description: 'description' } );
//     console.log( 'processed /products/' + req.params.id + ' request' );
// });
 
// app.listen( 3000 );
// console.log( 'Listening on port 3000...' );

var restify = require( 'restify' );
var MongoClient = require( 'mongodb' ).MongoClient;
var assert = require( 'assert' );

var _db = null;

var server = restify.createServer();

// Define API structure here
//
server.get( '/products', apiRequestProducts );
server.get( '/products/:id', apiRequestProducts );

// Connect to mongoDB
//
var uri = 'mongodb://app:app@localhost:27017/thingtracker';
MongoClient.connect( uri, function ( err, db ) {
	assert.equal( null, err );
	console.log( 'Connected to thingtracker database' );
	_db = db;

	server.listen( 3000, function () {
		console.log( '%s listening at %s', server.name, server.url );
	});
});

// Handler functions for DB access
//
function dbGetProductFromId( id, callback ) {
	var cursor = _db.collection( 'products' ).find( { '_id': id } );
	cursor.nextObject( function ( err, item ) {
		assert.equal( err, null );
		callback( item );
	});
}

// Handler functions for API calls
//
function apiRequestProducts( req, res, next ) {
	if ( req.params.id == undefined )
		res.send( [ { name: 'nonsense' }, { name: 'craziness' } ] );
	else {
		dbGetProductFromId( req.params.id, function ( result ) {
			if ( result == null )
				res.send( {} );
			else
				res.send( { id: result._id, name: result.name } );
		});
		
	}

	console.log( 'processed /products request with id = %s', req.params.id );
	next();
}