var app = angular.module("cartApp", [], function($interpolateProvider) {
	$interpolateProvider.startSymbol('[[');
	$interpolateProvider.endSymbol(']]');
});

app.controller("CartController", ['$scope', '$http', function($scope, $http) {
	// Initialize scope variables
	$scope.cartItems = [];
	$scope.subtotal = 0;
	$scope.checkoutError = "";
	$scope.selectAll = false; // Do not select all by default

	// Get accountId from the hidden input
	var accountIdElem = document.getElementById("accountId");
	var accountId = accountIdElem ? parseInt(accountIdElem.value) || 0 : 0;

	// Toggle "Select All" checkbox
	$scope.toggleSelectAll = function(selectAll) {
		$scope.cartItems.forEach(function(item) {
			item.selected = selectAll;
		});
		$scope.updateTotal();
	};

	// Update "Select All" status based on each item's selection
	$scope.updateSelectAllStatus = function() {
		$scope.selectAll = $scope.cartItems.length > 0 &&
			$scope.cartItems.every(function(item) { return item.selected; });
	};

	// Update total cost based on selected items
	$scope.updateTotal = function() {
		$scope.checkoutError = "";
		var total = 0;
		$scope.cartItems.forEach(function(item) {
			if (item.selected) {
				total += item.product.price * item.quantity;
			}
		});
		$scope.subtotal = total;
	};

	// Load cart items from the API without forcing selection
	$scope.loadCart = function() {
		if (accountId === 0) {
			$scope.cartItems = [];
			return;
		}
		$http.get("/api/cart/" + accountId)
			.then(function(response) {
				// Load items without altering each item's "selected" state
				$scope.cartItems = response.data;
				// Update "Select All" checkbox status based on current items
				$scope.updateSelectAllStatus();
				$scope.updateTotal();
				updateNavCount();
			})
			.catch(function(error) {
				console.error("Error loading cart:", error);
			});
	};

	// Update the quantity of a given cart item
	$scope.updateQuantity = function(cart, change) {
		$scope.checkoutError = "";
		if (cart.quantity + change < 1) return;
		var newQuantity = cart.quantity + change;
		var updatePayload = angular.copy(cart);
		updatePayload.quantity = newQuantity;
		$http.put("/api/cart/update", updatePayload)
			.then(function(response) {
				cart.quantity = newQuantity;
				$scope.updateTotal();
				updateNavCount();
			})
			.catch(function(error) {
				console.error("Error updating quantity:", error);
			});
	};

	// Remove an item from the cart and update local state
	$scope.removeItem = function(cart) {
		$http.delete("/api/cart/delete/" + cart.accountid + "/" + cart.productid)
			.then(function(response) {
				// Remove the item from the local array instead of reloading the entire cart
				var index = $scope.cartItems.indexOf(cart);
				if (index > -1) {
					$scope.cartItems.splice(index, 1);
				}
				// Update total and "Select All" status based on the new array
				$scope.updateTotal();
				$scope.updateSelectAllStatus();
				updateNavCount();
			})
			.catch(function(error) {
				console.error("Error removing item:", error);
			});
	};

	// Add a product to the cart
	$scope.addToCart = function(productId, productStock, productPrice) {
		if (productStock <= 0) {
			alert("Sản phẩm hết hàng!");
			return;
		}
		var payload = {
			productid: productId,
			accountid: accountId,
			quantity: 1
		};
		$http.post("/api/cart/add", payload)
			.then(function(response) {
				$scope.loadCart();
				updateNavCount();
			})
			.catch(function(error) {
				console.error("Error adding to cart:", error);
				alert("Không thể thêm vào giỏ hàng: " + error.data);
			});
	};

	// Checkout: build URL and redirect to OrderController
	$scope.checkout = function() {
		var selectedItems = $scope.cartItems.filter(function(item) {
			return item.selected;
		}).map(function(item) {
			return item.productid;
		});
		if (selectedItems.length === 0) {
			$scope.checkoutError = "Vui lòng chọn ít nhất một sản phẩm để thanh toán!";
			return;
		}
		var url = "/order/checkout?accountId=" + accountId;
		selectedItems.forEach(function(id) {
			url += "&selectedCartItems=" + id;
		});
		window.location.href = url;
	};

	// Update the navigation cart count (if applicable)
	function updateNavCount() {
		if (accountId !== 0) {
			$http.get("/api/cart/" + accountId)
				.then(function(response) {
					var items = response.data;
					var count = 0;
					items.forEach(function(item) {
						count += item.quantity;
					});
					var navCountElem = document.getElementById("cartItemCount");
					if (navCountElem) {
						navCountElem.textContent = count;
					}
				})
				.catch(function(error) {
					console.error("Error updating nav count:", error);
				});
		}
	}

	// Load cart items when the controller initializes
	$scope.loadCart();
}]);
