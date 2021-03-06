/**
 * Created by kweinberger on 06.12.2018.
 */

import { details, mergeInEmptyDraft } from "../detail-definitions.js";

export const booksSearch = {
  identifier: "booksSearch",
  label: "Find a Book",
  icon: "#ico36_plus",
  draft: {
    ...mergeInEmptyDraft({
      content: {
        type: ["demo:BookSearch"],
      },
      seeks: {
        type: ["demo:BookOffer"],
      },
    }),
  },
  reactionUseCases: ["booksSearch"],
  seeksDetails: {
    title: { ...details.title, mandatory: true },
    author: { ...details.author },
    isbn: { ...details.isbn },
    description: { ...details.description },
    price: { ...details.price },
    tags: { ...details.tags },
    location: { ...details.location },
    images: { ...details.images },
    website: { ...details.website },
  },
};
