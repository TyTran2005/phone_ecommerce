var app = angular.module("accountApp", [], function($interpolateProvider) {
	$interpolateProvider.startSymbol('[[');
	$interpolateProvider.endSymbol(']]');
});

app.controller("AccountController", ['$scope', '$http', function($scope, $http) {
	$scope.accounts = [];
	$scope.item = {}; // Object for the account form
	$scope.error = "";

	// Load all accounts
	$scope.loadAccounts = function() {
		$http.get("/api/account")
			.then(function(response) {
				$scope.accounts = response.data;
			})
			.catch(function(error) {
				console.error("Error loading accounts:", error);
				$scope.error = error.data;
			});
	};

	// Create a new account
	$scope.createAccount = function() {
		// Validate required fields
		if (!$scope.item.username || !$scope.item.password || !$scope.item.email) {
			$scope.error = "Username, password, and email are required!";
			return;
		}
		var formData = new FormData();
		formData.append("username", $scope.item.username);
		formData.append("password", $scope.item.password);
		formData.append("email", $scope.item.email);
		formData.append("fullname", $scope.item.fullname || "");
		formData.append("admin", $scope.item.admin);
		formData.append("active", $scope.item.active);

		$http.post("/api/account", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadAccounts();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error creating account:", error);
			$scope.error = error.data;
		});
	};

	// Update an existing account
	$scope.updateAccount = function() {
		if (!$scope.item.id) {
			$scope.error = "Account id is required for update!";
			return;
		}
		var formData = new FormData();
		formData.append("id", $scope.item.id);
		formData.append("username", $scope.item.username);
		formData.append("password", $scope.item.password);
		formData.append("email", $scope.item.email);
		formData.append("fullname", $scope.item.fullname || "");
		formData.append("admin", $scope.item.admin);
		formData.append("active", $scope.item.active);

		$http.put("/api/account", formData, {
			transformRequest: angular.identity,
			headers: { 'Content-Type': undefined }
		}).then(function(response) {
			$scope.loadAccounts();
			$scope.resetForm();
		}).catch(function(error) {
			console.error("Error updating account:", error);
			$scope.error = error.data;
		});
	};

	// Delete an account
	$scope.deleteAccount = function(id) {
		$http.delete("/api/account/" + id)
			.then(function(response) {
				$scope.loadAccounts();
			})
			.catch(function(error) {
				console.error("Error deleting account:", error);
				$scope.error = error.data;
			});
	};

	// Edit an account: copy its data into the form model
	$scope.editAccount = function(acc) {
		$scope.item = angular.copy(acc);
	};

	// Reset the form
	$scope.resetForm = function() {
		$scope.item = {};
		$scope.error = "";
		document.getElementById('accountForm').reset();
	};

	// Initialize by loading accounts
	$scope.loadAccounts();
}]);
