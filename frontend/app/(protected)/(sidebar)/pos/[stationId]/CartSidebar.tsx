"use client";

import {
  ShoppingCart,
  DollarSign,
  User,
  Plus,
  Minus,
  Trash2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { CartItem, CurrentUser } from "./types";

interface CartSidebarProps {
  cart: CartItem[];
  currentUser: CurrentUser | null;
  isProcessingSale: boolean;
  onUpdateQuantity: (productId: number, change: number) => void;
  onRemoveItem: (productId: number) => void;
  onProcessSale: () => void;
  onClearCart: () => void;
}

export function CartSidebar({
  cart,
  currentUser,
  isProcessingSale,
  onUpdateQuantity,
  onRemoveItem,
  onProcessSale,
  onClearCart,
}: CartSidebarProps) {
  const cartTotal = cart.reduce(
    (sum, item) => sum + item.quantity * item.unitPrice,
    0
  );

  return (
    <div className="w-full lg:w-80 bg-card rounded-lg shadow-sm p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold text-gray-100">Cart</h2>
        <DollarSign className="w-6 h-6 text-green-400" />
      </div>

      {currentUser && (
        <div className="mb-4 p-2 bg-gray-800 rounded text-xs text-gray-400">
          <span className="flex items-center gap-1">
            <User className="w-3 h-3" />
            Sale by: {currentUser.name || currentUser.email}
          </span>
        </div>
      )}

      {cart.length === 0 ? (
        <div className="text-center py-8 text-gray-400">
          <ShoppingCart className="w-12 h-12 mx-auto mb-2 opacity-50" />
          <p>Your cart is empty</p>
          <p className="text-sm">Tap products to add them</p>
        </div>
      ) : (
        <>
          <div className="space-y-3 mb-6 max-h-64 overflow-y-auto">
            {cart.map((item) => (
              <div
                key={item.productId}
                className="flex items-center justify-between p-2 bg-gray-800 rounded"
              >
                <div className="flex-1">
                  <p className="font-medium text-gray-100 truncate">
                    {item.productName}
                  </p>
                  <p className="text-sm text-gray-400">
                    ${item.unitPrice} × {item.quantity} = $
                    {(item.unitPrice * item.quantity).toFixed(2)}
                  </p>
                </div>
                <div className="flex items-center gap-2 ml-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={(e) => {
                      e.stopPropagation();
                      onUpdateQuantity(item.productId, -1);
                    }}
                    className="h-6 w-6 p-0"
                  >
                    <Minus className="w-3 h-3" />
                  </Button>
                  <span className="text-sm w-6 text-center">
                    {item.quantity}
                  </span>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={(e) => {
                      e.stopPropagation();
                      onUpdateQuantity(item.productId, 1);
                    }}
                    className="h-6 w-6 p-0"
                    disabled={item.quantity >= item.maxQuantity}
                  >
                    <Plus className="w-3 h-3" />
                  </Button>
                  <Button
                    size="sm"
                    variant="destructive"
                    onClick={(e) => {
                      e.stopPropagation();
                      onRemoveItem(item.productId);
                    }}
                    className="h-6 w-6 p-0 ml-1"
                  >
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </div>
            ))}
          </div>

          <div className="border-t border-gray-600 pt-4 mb-4">
            <div className="flex justify-between text-lg font-bold text-gray-100">
              <span>Total:</span>
              <span>${cartTotal.toFixed(2)}</span>
            </div>
          </div>

          <div className="space-y-2">
            <Button
              onClick={onProcessSale}
              disabled={isProcessingSale}
              className="w-full bg-green-600 hover:bg-green-700 text-white"
            >
              {isProcessingSale ? "Processing..." : "Sell"}
            </Button>
            <Button onClick={onClearCart} variant="outline" className="w-full">
              Cancel
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
