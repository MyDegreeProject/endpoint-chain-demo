import defaultSettings from "@/settings";

const title = defaultSettings.title || "Federation Endpoint Chain";

export default function getPageTitle(pageTitle) {
  if (pageTitle) {
    return `${pageTitle} - ${title}`;
  }
  return `${title}`;
}
