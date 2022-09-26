const busImg = "https://upload.wikimedia.org/wikipedia/commons/e/e6/Bus-logo.svg";
const userIcon = "https://seekicon.com/free-icon-download/user_1.svg";
let map;
function initMap() {
	const busIcon = {
		url: busImg, // url
		scaledSize: new google.maps.Size(40, 40), // scaled size

	};

	const locationIcon = {
		url: "https://seekicon.com/free-icon-download/user_1.svg", // url
		scaledSize: new google.maps.Size(30, 30),
		// scaled size
	};

	map = new google.maps.Map(document.getElementById('map'), {
		center: { lat: parseFloat(userLocation.lat), lng: parseFloat(userLocation.lng) }, //center on bus location currently
		zoom: 15,
		scrollwheel: false
	});


	console.log(userLocation);



	for (let i = 0; i < busLocations.length; i++) { //initialize markers on all nearby buses
		let marker = new google.maps.Marker({
			position: { lat: parseFloat(busLocations[i].LATITUDE), lng: parseFloat(busLocations[i].LONGITUDE) },
			map: map,
			icon: busIcon,
		});


		let contentString = `<h3>Bus #${busLocations[i].VEHICLE}</h3>`

		let infoWindow = new google.maps.InfoWindow({
			content: contentString
		});

		google.maps.event.addListener(marker, 'click',
			function() {
				infoWindow.open(map, marker)
			})

	}

	let locationMarker = new google.maps.Marker({
		position: { lat: parseFloat(userLocation.lat), lng: parseFloat(userLocation.lng) },
		map: map,
		icon: locationIcon,
		animation: google.maps.Animation.BOUNCE,
	})
}