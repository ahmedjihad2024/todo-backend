# todo API — Endpoint Report

**Total: 32 controllers · ~80 endpoints** · Base convention: `/api/...` · Auth via role gates (`USER`, `PROVIDER`, `ADMIN`)

> The `booking` module is still under development and has no controller wired up yet, so it exposes no endpoints.

## 🔐 Authentication
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| POST | `/api/auth/verify-phone` | Verify phone (OTP) → authenticated | Public |
| POST | `/api/auth/verify-phone/resend` | Resend verification code | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| POST | `/api/auth/logout` | Logout | Authenticated |
| POST | `/api/user/auth/register` | User register | Public |
| POST | `/api/user/auth/login` | User login | Public |
| POST | `/api/provider/auth/register` | Provider register | Public |
| POST | `/api/provider/auth/login` | Provider login | Public |

## 👤 Profile / Account
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| POST | `/api/profile/me/change-phone/request` | Request phone change | Authenticated |
| POST | `/api/profile/me/change-phone/verify` | Verify phone change code | Authenticated |
| GET | `/api/user/profile/me` | Get my profile | USER |
| PATCH | `/api/user/profile/me` | Update profile | USER |
| DELETE | `/api/user/profile/me` | Delete account | USER |
| POST | `/api/user/profile/me/avatar` | Upload avatar (multipart) | USER |
| GET | `/api/provider/profile/me` | Get my profile | PROVIDER |
| PATCH | `/api/provider/profile/me` | Update profile | PROVIDER |
| DELETE | `/api/provider/profile/me` | Delete account | PROVIDER |
| POST | `/api/provider/profile/me/avatar` | Upload avatar (multipart) | PROVIDER |

## 🛡️ Admin
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/admin/users` | List users | ADMIN |
| PATCH | `/api/admin/users/{id}/roles` | Update user roles | ADMIN |

## 🏪 Stores
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/stores` | List stores | USER |
| GET | `/api/user/stores/{id}` | Get store | USER |
| GET | `/api/provider/stores` | My stores | PROVIDER |
| GET | `/api/provider/stores/{id}` | My store | PROVIDER |
| POST | `/api/provider/stores` | Create store (multipart) | PROVIDER |
| PATCH | `/api/provider/stores/{id}` | Update store (multipart) | PROVIDER |
| PATCH | `/api/provider/stores/{id}/hide` | Hide store | PROVIDER |
| PATCH | `/api/provider/stores/{id}/show` | Show store | PROVIDER |
| GET | `/api/user/store/home` | Store home feed | USER |

## 📦 Store Sections
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/stores/{storeId}/sections` | List sections | USER |
| GET | `/api/provider/stores/{storeId}/sections` | List sections | PROVIDER |
| POST | `/api/provider/stores/{storeId}/sections` | Create section | PROVIDER |
| PATCH | `/api/provider/stores/{storeId}/sections/{sectionId}` | Update section | PROVIDER |
| DELETE | `/api/provider/stores/{storeId}/sections/{sectionId}` | Delete section | PROVIDER |

## 🛍️ Products
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/products` | Search products | USER |
| GET | `/api/user/products/{id}` | Get product | USER |
| GET | `/api/provider/products` | List products | PROVIDER |
| POST | `/api/provider/products` | Create product (multipart) | PROVIDER |
| PATCH | `/api/provider/products/{productId}` | Update product (multipart) | PROVIDER |
| DELETE | `/api/provider/products/{productId}` | Delete product | PROVIDER |

## ❤️ Favorites
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/favorite` | List favorites | USER |
| POST | `/api/user/favorite/{productId}` | Toggle favorite | USER |

## 🛒 Cart
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/cart` | Get cart | USER |
| PATCH | `/api/user/cart/items/{productId}` | Set item quantity | USER |
| DELETE | `/api/user/cart/items/{productId}` | Remove item | USER |
| DELETE | `/api/user/cart` | Clear cart | USER |

## 📑 Orders
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| POST | `/api/user/order/checkout` | Checkout | USER |
| GET | `/api/user/order` | List orders | USER |
| GET | `/api/user/order/{orderId}` | Get order | USER |
| PATCH | `/api/user/order/{orderId}` | Update order | USER |
| POST | `/api/user/order/{orderId}/cancel` | Cancel order | USER |
| GET | `/api/provider/order` | List orders | PROVIDER |
| GET | `/api/provider/order/{orderId}` | Get order | PROVIDER |
| PATCH | `/api/provider/order/{orderId}/status` | Update order status | PROVIDER |

## 🏟️ Stadiums
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/user/stadiums` | List stadiums | USER |
| GET | `/api/user/stadiums/{id}` | Get stadium | USER |
| GET | `/api/provider/stadiums` | My stadiums | PROVIDER |
| GET | `/api/provider/stadiums/{id}` | My stadium | PROVIDER |
| POST | `/api/provider/stadiums` | Create stadium (multipart) | PROVIDER |
| PATCH | `/api/provider/stadiums/{id}` | Update stadium (multipart) | PROVIDER |
| DELETE | `/api/provider/stadiums/{id}` | Delete stadium | PROVIDER |
| PATCH | `/api/provider/stadiums/{id}/hide` | Hide stadium | PROVIDER |
| PATCH | `/api/provider/stadiums/{id}/show` | Show stadium | PROVIDER |

## 🏅 Sports
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/sports` | List sports | Public |
| GET | `/api/user/sports/favorites` | Get favorite sports | USER |
| PUT | `/api/user/sports/favorites` | Set favorite sports | USER |
| POST | `/api/admin/sports` | Create sport (multipart) | ADMIN |
| PATCH | `/api/admin/sports/{id}` | Update sport (multipart) | ADMIN |
| DELETE | `/api/admin/sports/{id}` | Delete sport | ADMIN |

## 🗂️ Categories
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/categories` | List categories | Public |
| POST | `/api/admin/categories` | Create category (multipart) | ADMIN |
| PATCH | `/api/admin/categories/{id}` | Update category (multipart) | ADMIN |
| DELETE | `/api/admin/categories/{id}` | Delete category | ADMIN |

## 🧰 Services
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/services` | List services | Public |
| POST | `/api/admin/services` | Create service (multipart) | ADMIN |
| PATCH | `/api/admin/services/{id}` | Update service (multipart) | ADMIN |
| DELETE | `/api/admin/services/{id}` | Delete service | ADMIN |

## 📍 Locations
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/api/locations` | List governorates + cities | Public |
| POST | `/api/admin/locations/governorates` | Create governorate | ADMIN |
| PATCH | `/api/admin/locations/governorates/{id}` | Update governorate | ADMIN |
| DELETE | `/api/admin/locations/governorates/{id}` | Delete governorate | ADMIN |
| POST | `/api/admin/locations/governorates/{governorateId}/cities` | Create city | ADMIN |
| PATCH | `/api/admin/locations/cities/{id}` | Update city | ADMIN |
| DELETE | `/api/admin/locations/cities/{id}` | Delete city | ADMIN |

## 🖼️ Media / Misc
| Method | Path | Handler | Auth |
|--------|------|---------|------|
| GET | `/media/{category}/{filename}` | Serve media file | Public |
| GET | `/api/stores/{id}/tax` | Store tax info | PROVIDER / ADMIN |
| GET | `/api/stadiums/{id}/tax` | Stadium tax info | PROVIDER / ADMIN |
