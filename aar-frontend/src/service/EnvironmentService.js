class EnvironmentService {
  static INSTANCE = new EnvironmentService();

  static getInstance () {
    return EnvironmentService.INSTANCE;
  }

  isLocalDevelopment() {
    const host = window.location.host;
    return (host === "localhost:8080");
  }
}

const environment = EnvironmentService.getInstance();

export {EnvironmentService, environment};
