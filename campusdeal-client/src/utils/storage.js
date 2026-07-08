const userStorage = {
  get(key) {
    return sessionStorage.getItem(key);
  },
  set(key, value) {
    sessionStorage.setItem(key, value);
  },
  remove(key) {
    sessionStorage.removeItem(key);
  },
  clear() {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('refreshToken');
    sessionStorage.removeItem('userInfo');
    sessionStorage.removeItem('userRole');
  }
};

const adminStorage = {
  get(key) {
    return localStorage.getItem(key);
  },
  set(key, value) {
    localStorage.setItem(key, value);
  },
  remove(key) {
    localStorage.removeItem(key);
  },
  clear() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminInfo');
    localStorage.removeItem('adminRole');
  }
};

export { userStorage, adminStorage };
