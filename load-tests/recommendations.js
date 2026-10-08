import http, { expectedStatuses } from "k6/http";
import { check } from "k6";
import { Counter } from "k6/metrics";

const acceptedRequests = new Counter("accepted_requests");
const rateLimitedRequests = new Counter("rate_limited_requests");

http.setResponseCallback(expectedStatuses(200, 429));

export const options = {
  scenarios: {
    recommendationBurst: {
      executor: "shared-iterations",
      vus: 10,
      iterations: 40,
      maxDuration: "30s",
    },
  },

  thresholds: {
    checks: ["rate == 1"],
    http_req_duration: ["p(95) < 1000"],
    accepted_requests: ["count > 0"],
    rate_limited_requests: ["count > 0"],
  },
};

const baseUrl =
  __ENV.BASE_URL || "http://host.docker.internal:8080";

export default function () {
  const response = http.get(
    `${baseUrl}/api/destinations/recommendations` +
      "?budget=1000" +
      "&climate=WARM" +
      "&maxFlightTimeHours=5" +
      "&interests=food" +
      "&interests=history" +
      "&limit=5",
    {
      tags: {
        endpoint: "recommendations",
      },
    }
  );

  if (response.status === 200) {
    acceptedRequests.add(1);
  }

  if (response.status === 429) {
    rateLimitedRequests.add(1);
  }

  check(response, {
    "returns 200 or controlled 429": (result) =>
      result.status === 200 || result.status === 429,

    "includes rate-limit capacity": (result) =>
      result.headers["X-Ratelimit-Limit"] === "30",

    "includes remaining request count": (result) =>
      result.headers["X-Ratelimit-Remaining"] !== undefined,

    "429 includes retry guidance": (result) =>
      result.status !== 429 ||
      result.headers["Retry-After"] !== undefined,
  });
}